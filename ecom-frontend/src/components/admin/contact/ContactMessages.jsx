import { useEffect, useMemo, useState } from "react";
import { DataGrid } from "@mui/x-data-grid";
import { FaEnvelopeOpenText, FaEye, FaInbox, FaTrashAlt } from "react-icons/fa";
import toast from "react-hot-toast";
import api from "../../../api/api";
import Loader from "../../shared/Loader";
import ErrorPage from "../../shared/ErrorPage";
import Modal from "../../shared/Modal";
import DeleteModal from "../../shared/DeleteModal";

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat(undefined, { dateStyle: "medium", timeStyle: "short" }).format(new Date(value))
  : "—";

const ContactMessages = () => {
  const [messages, setMessages] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedMessage, setSelectedMessage] = useState(null);
  const [viewOpen, setViewOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const loadMessages = async () => {
    setIsLoading(true);
    setError("");
    try {
      const { data } = await api.get("/admin/contact-messages");
      setMessages(data);
    } catch (requestError) {
      setError(requestError?.response?.data?.message || "Unable to load contact messages.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadMessages();
  }, []);

  const viewMessage = async (messageId) => {
    try {
      const { data } = await api.get(`/admin/contact-messages/${messageId}`);
      setSelectedMessage(data);
      setViewOpen(true);
    } catch (requestError) {
      toast.error(requestError?.response?.data?.message || "Unable to load this message.");
    }
  };

  const deleteMessage = async () => {
    if (!selectedMessage?.contactMessageId) return;

    setIsDeleting(true);
    try {
      const { data } = await api.delete(`/admin/contact-messages/${selectedMessage.contactMessageId}`);
      setMessages((currentMessages) => currentMessages.filter(
        (message) => message.contactMessageId !== selectedMessage.contactMessageId
      ));
      toast.success(data?.message || "Contact message deleted successfully.");
      setDeleteOpen(false);
      setSelectedMessage(null);
    } catch (requestError) {
      toast.error(requestError?.response?.data?.message || "Unable to delete this message.");
    } finally {
      setIsDeleting(false);
    }
  };

  const columns = useMemo(() => [
    {
      field: "name",
      headerName: "Name",
      minWidth: 180,
      flex: 1,
      headerAlign: "center",
      align: "center",
      cellClassName: "font-medium text-slate-700",
    },
    {
      field: "email",
      headerName: "Email",
      minWidth: 260,
      flex: 1,
      headerAlign: "center",
      align: "center",
      renderCell: (params) => <span className="break-all text-center">{params.value}</span>,
    },
    {
      field: "message",
      headerName: "Message",
      minWidth: 320,
      flex: 2,
      headerAlign: "center",
      renderCell: (params) => <span className="line-clamp-2 whitespace-normal">{params.value}</span>,
    },
    {
      field: "createdAt",
      headerName: "Date & Time",
      minWidth: 190,
      headerAlign: "center",
      align: "center",
      valueFormatter: (value) => formatDateTime(value),
    },
    {
      field: "actions",
      headerName: "Actions",
      minWidth: 190,
      sortable: false,
      filterable: false,
      headerAlign: "center",
      align: "center",
      renderCell: (params) => (
        <div className="flex h-full items-center justify-center gap-2">
          <button
            type="button"
            onClick={() => viewMessage(params.row.contactMessageId)}
            className="flex h-9 items-center gap-1 rounded-md bg-blue-500 px-3 text-sm font-medium text-white hover:bg-blue-600"
          >
            <FaEye /> View
          </button>
          <button
            type="button"
            onClick={() => {
              setSelectedMessage(params.row);
              setDeleteOpen(true);
            }}
            className="flex h-9 items-center gap-1 rounded-md bg-red-500 px-3 text-sm font-medium text-white hover:bg-red-600"
          >
            <FaTrashAlt /> Delete
          </button>
        </div>
      ),
    },
  ], []);

  if (isLoading) return <Loader text="Loading contact messages..." />;
  if (error) return <ErrorPage message={error} />;

  return (
    <div className="pb-6 pt-6">
      <div className="mb-6 flex items-center justify-center gap-3">
        <FaEnvelopeOpenText className="text-3xl text-custom-blue" />
        <h1 className="text-center text-3xl font-bold uppercase text-slate-800">Contact Messages</h1>
      </div>

      {messages.length === 0 ? (
        <div className="flex flex-col items-center justify-center rounded-lg border border-slate-200 py-16 text-center text-gray-600">
          <FaInbox size={52} className="mb-4 text-slate-400" />
          <h2 className="text-2xl font-semibold text-slate-800">No contact messages yet</h2>
          <p className="mt-2">New customer messages will appear here.</p>
        </div>
      ) : (
        <div className="w-full overflow-x-auto">
          <DataGrid
            className="min-w-[1140px]"
            rows={messages}
            columns={columns}
            getRowId={(row) => row.contactMessageId}
            disableRowSelectionOnClick
            disableColumnResize
            autoHeight
            pageSizeOptions={[10, 25, 50]}
            initialState={{ pagination: { paginationModel: { pageSize: 10, page: 0 } } }}
            pagination
          />
        </div>
      )}

      <Modal open={viewOpen} setOpen={setViewOpen} title="Contact Message">
        {selectedMessage && (
          <div className="space-y-5 break-words text-slate-700">
            <div className="grid gap-4 sm:grid-cols-2">
              <div><p className="text-sm font-medium text-slate-500">Name</p><p className="font-semibold">{selectedMessage.name}</p></div>
              <div><p className="text-sm font-medium text-slate-500">Email</p><p className="break-all font-semibold">{selectedMessage.email}</p></div>
            </div>
            <div>
              <p className="text-sm font-medium text-slate-500">Received</p>
              <p className="font-semibold">{formatDateTime(selectedMessage.createdAt)}</p>
            </div>
            <div>
              <p className="mb-2 text-sm font-medium text-slate-500">Message</p>
              <p className="whitespace-pre-wrap rounded-md border border-slate-200 bg-slate-50 p-4 leading-7">{selectedMessage.message}</p>
            </div>
          </div>
        )}
      </Modal>

      <DeleteModal
        open={deleteOpen}
        setOpen={setDeleteOpen}
        loader={isDeleting}
        title="Delete Contact Message"
        onDeleteHandler={deleteMessage}
      />
    </div>
  );
};

export default ContactMessages;
