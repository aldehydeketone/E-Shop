package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.CartItemDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CartServiceImpl implements CartService{
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);
    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    @Transactional
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart  = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setTotalPrice(cart.getTotalPrice());

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = toProductDTO(item.getProduct());
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = new CartDTO();
            cartDTO.setCartId(cart.getCartId());
            cartDTO.setTotalPrice(cart.getTotalPrice());

            List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = toProductDTO(cartItem.getProduct());
                productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                return productDTO;
            }).collect(Collectors.toList());


            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override
    @Transactional
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null){
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        CartDTO cartDTO = new CartDTO();
        cartDTO.setCartId(cart.getCartId());
        cartDTO.setTotalPrice(cart.getTotalPrice());
        cart.getCartItems().forEach(c ->
                c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> toProductDTO(p.getProduct()))
                .toList();
        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        log.info("UPDATE_CART STEP 1 - Enter updateProductQuantityInCart with productId={}, quantity={}", productId, quantity);
        try {
            log.info("UPDATE_CART STEP 2 - Before createCart()");
            Cart userCart = createCart();
            log.info("UPDATE_CART STEP 3 - After createCart(), userCartId={}", userCart != null ? userCart.getCartId() : null);

            Long cartId = userCart.getCartId();

            log.info("UPDATE_CART STEP 4 - Before cartRepository.findById(cartId={})", cartId);
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
            log.info("UPDATE_CART STEP 5 - After find cart: cartId={}", cart.getCartId());

            log.info("UPDATE_CART STEP 6 - Before productRepository.findById(productId={})", productId);
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
            log.info("UPDATE_CART STEP 7 - After find product: productId={}, name={}", product.getProductId(), product.getProductName());

            if (product.getQuantity() == 0) {
                throw new APIException(product.getProductName() + " is not available");
            }

            if (product.getQuantity() < quantity) {
                throw new APIException("Please, make an order of the " + product.getProductName()
                        + " less than or equal to the quantity " + product.getQuantity() + ".");
            }

            log.info("UPDATE_CART STEP 8 - Before findCartItemByProductIdAndCartId(cartId={}, productId={})", cartId, productId);
            CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
            log.info("UPDATE_CART STEP 9 - After find cartItem: cartItem={}", cartItem != null ? cartItem.getCartItemId() : "null");

            if (cartItem == null) {
                throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
            }

            log.info("UPDATE_CART STEP 10 - Before calculate newQuantity (current item qty={}, requested qty={})", cartItem.getQuantity(), quantity);
            int newQuantity = cartItem.getQuantity() + quantity;
            log.info("UPDATE_CART STEP 11 - After calculate newQuantity={}", newQuantity);

            if (newQuantity < 0) {
                throw new APIException("The resulting quantity cannot be negative.");
            }

            if (newQuantity == 0) {
                log.info("UPDATE_CART STEP 12 - Before deleteProductFromCart(cartId={}, productId={})", cartId, productId);
                deleteProductFromCart(cartId, productId);
                log.info("UPDATE_CART STEP 13 - After deleteProductFromCart()");
            } else {
                cartItem.setProductPrice(product.getSpecialPrice());
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                cartItem.setDiscount(product.getDiscount());
                cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));

                log.info("UPDATE_CART STEP 14 - Before cartRepository.save(cart)");
                cartRepository.save(cart);
                log.info("UPDATE_CART STEP 15 - After cartRepository.save(cart)");
            }

            log.info("UPDATE_CART STEP 16 - Before cartItemRepository.save(cartItem)");
            CartItem updatedItem = cartItemRepository.save(cartItem);
            log.info("UPDATE_CART STEP 17 - After cartItemRepository.save(cartItem), updatedItem qty={}", updatedItem != null ? updatedItem.getQuantity() : "null");

            if (updatedItem != null && updatedItem.getQuantity() == 0) {
                log.info("UPDATE_CART STEP 18 - Before cartItemRepository.deleteById(cartItemId={})", updatedItem.getCartItemId());
                cartItemRepository.deleteById(updatedItem.getCartItemId());
                log.info("UPDATE_CART STEP 19 - After cartItemRepository.deleteById()");
            }

            log.info("UPDATE_CART STEP 20 - Before building CartDTO");
            CartDTO cartDTO = new CartDTO();
            cartDTO.setCartId(cart.getCartId());
            cartDTO.setTotalPrice(cart.getTotalPrice());

            log.info("UPDATE_CART STEP 21 - Before cart.getCartItems()");
            List<CartItem> cartItems = cart.getCartItems();
            log.info("UPDATE_CART STEP 22 - After cart.getCartItems(), count={}", cartItems != null ? cartItems.size() : "null");

            log.info("UPDATE_CART STEP 23 - Before mapping productStream");
            Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
                ProductDTO prd = toProductDTO(item.getProduct());
                prd.setQuantity(item.getQuantity());
                return prd;
            });

            cartDTO.setProducts(productStream.toList());
            log.info("UPDATE_CART STEP 24 - After building CartDTO, before return");

            return cartDTO;
        } catch (Exception e) {
            log.error("UPDATE CART FAILED", e);
            throw e;
        }
    }


    private Cart createCart() {
        Cart userCart  = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart =  cartRepository.save(cart);

        return newCart;
    }


    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }


    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {
        // Get user's email
        String emailId = authUtil.loggedInEmail();

        // Check if an existing cart is available or create a new one
        Cart existingCart = cartRepository.findCartByEmail(emailId);
        if (existingCart == null) {
            existingCart = new Cart();
            existingCart.setTotalPrice(0.00);
            existingCart.setUser(authUtil.loggedInUser());
            existingCart = cartRepository.save(existingCart);
        } else {
            // Clear all current items in the existing cart
            cartItemRepository.deleteAllByCartId(existingCart.getCartId());
        }

        double totalPrice = 0.00;

        // Process each item in the request to add to the cart
        for (CartItemDTO cartItemDTO : cartItems) {
            Long productId = cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();

            // Find the product by ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

            // Directly update product stock and total price
            // product.setQuantity(product.getQuantity() - quantity);
            totalPrice += product.getSpecialPrice() * quantity;

            // Create and save cart item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(existingCart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);
        }

        // Update the cart's total price and save
        existingCart.setTotalPrice(totalPrice);
        cartRepository.save(existingCart);
        return "Cart created/updated with the new items successfully";
    }

    private ProductDTO toProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setProductName(product.getProductName());
        productDTO.setImage(product.getImage());
        productDTO.setDescription(product.getDescription());
        productDTO.setQuantity(product.getQuantity());
        productDTO.setPrice(product.getPrice());
        productDTO.setDiscount(product.getDiscount());
        productDTO.setSpecialPrice(product.getSpecialPrice());
        return productDTO;
    }
}
