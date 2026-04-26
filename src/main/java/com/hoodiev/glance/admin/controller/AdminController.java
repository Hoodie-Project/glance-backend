package com.hoodiev.glance.admin.controller;

import com.hoodiev.glance.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getStats());
        return "admin/dashboard";
    }

    @GetMapping("/threads")
    public String threads(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean showDeleted,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        model.addAttribute("threads", adminService.getThreads(keyword, showDeleted, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("showDeleted", showDeleted != null && showDeleted);
        return "admin/threads";
    }

    @GetMapping("/threads/{id}")
    public String threadDetail(@PathVariable Long id, Model model) {
        model.addAttribute("thread", adminService.getThread(id));
        model.addAttribute("comments", adminService.getCommentsByThread(id));
        return "admin/thread-detail";
    }

    @PostMapping("/threads/{id}/delete")
    public String forceDeleteThread(@PathVariable Long id, RedirectAttributes ra) {
        adminService.forceDeleteThread(id);
        ra.addFlashAttribute("message", "게시글이 완전 삭제됐습니다.");
        return "redirect:/admin/threads";
    }

    @PostMapping("/threads/{id}/restore")
    public String restoreThread(@PathVariable Long id, RedirectAttributes ra) {
        adminService.restoreThread(id);
        ra.addFlashAttribute("message", "게시글이 복구됐습니다.");
        return "redirect:/admin/threads";
    }

    @GetMapping("/comments")
    public String comments(@PageableDefault(size = 20) Pageable pageable, Model model) {
        model.addAttribute("comments", adminService.getComments(pageable));
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes ra) {
        adminService.deleteComment(id);
        ra.addFlashAttribute("message", "댓글이 삭제됐습니다.");
        return "redirect:/admin/comments";
    }

    @GetMapping("/regions")
    public String regions(Model model) {
        model.addAttribute("regions", adminService.getRegions());
        return "admin/regions";
    }
}
