package com.ecommerce.project.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Temporary diagnostic endpoint – REMOVE after investigation is complete.
 * Public (no auth) so it can be checked from a browser or curl.
 */
@RestController
@RequestMapping("/api/public/diag")
public class DiagController {

    @Value("${project.image}")
    private String projectImagePath;

    @Value("${image.base.url}")
    private String imageBaseUrl;

    // Git info baked in at build time by git-commit-id-maven-plugin
    @Value("${git.commit.id.abbrev:UNKNOWN}")
    private String gitCommitShort;

    @Value("${git.commit.id:UNKNOWN}")
    private String gitCommitFull;

    @Value("${git.branch:UNKNOWN}")
    private String gitBranch;

    @Value("${git.commit.message.short:UNKNOWN}")
    private String gitCommitMessage;

    @Value("${git.commit.time:UNKNOWN}")
    private String gitCommitTime;

    @GetMapping
    public ResponseEntity<Map<String, Object>> diag() {
        Map<String, Object> info = new LinkedHashMap<>();

        // ── Git / deployment proof ──────────────────────────────────────────
        info.put("git_branch",              gitBranch);
        info.put("git_commit_short",        gitCommitShort);
        info.put("git_commit_full",         gitCommitFull);
        info.put("git_commit_message",      gitCommitMessage);
        info.put("git_commit_time",         gitCommitTime);

        // ── Config values ──────────────────────────────────────────────
        info.put("project.image_property",       projectImagePath);
        info.put("image.base.url_property",      imageBaseUrl);

        // ── JVM / OS context ───────────────────────────────────────────
        info.put("jvm_working_dir",              System.getProperty("user.dir"));
        info.put("java.io.tmpdir",               System.getProperty("java.io.tmpdir"));
        info.put("os.name",                      System.getProperty("os.name"));

        // ── Resolved directory ─────────────────────────────────────────
        File absDir = Paths.get(projectImagePath).toAbsolutePath().toFile();
        info.put("resolved_absolute_path",       absDir.getAbsolutePath());
        info.put("dir_exists",                   absDir.exists());
        info.put("dir_isDirectory",              absDir.isDirectory());
        info.put("dir_canWrite",                 absDir.canWrite());

        // try creating the directory
        boolean mkdirsResult = absDir.mkdirs();
        info.put("mkdirs_called_result",         mkdirsResult);
        info.put("dir_exists_after_mkdirs",      absDir.exists());

        // list files if directory exists
        if (absDir.exists() && absDir.isDirectory()) {
            String[] files = absDir.list();
            info.put("files_in_dir_count",       files != null ? files.length : 0);
            info.put("files_in_dir",             files);
        }

        // ── WebMvcConfig would serve from ─────────────────────────────
        // WebMvcConfig uses: "file:" + projectImagePath + "/"
        info.put("webmvc_resource_location",     "file:" + projectImagePath + "/");
        info.put("webmvc_resolved_absolute",     "file:" + absDir.getAbsolutePath() + "/");

        return ResponseEntity.ok(info);
    }
}
