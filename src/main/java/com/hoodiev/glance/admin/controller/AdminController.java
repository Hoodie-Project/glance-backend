package com.hoodiev.glance.admin.controller;

import com.hoodiev.glance.admin.service.AdminService;
import com.hoodiev.glance.report.entity.ReportStatus;
import com.hoodiev.glance.report.entity.ReportTargetType;
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
    public String comments(
            @RequestParam(required = false) Boolean showDeleted,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        model.addAttribute("comments", adminService.getComments(showDeleted, pageable));
        model.addAttribute("showDeleted", showDeleted != null && showDeleted);
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes ra) {
        adminService.deleteComment(id);
        ra.addFlashAttribute("message", "댓글이 삭제됐습니다.");
        return "redirect:/admin/comments";
    }

    @PostMapping("/comments/{id}/restore")
    public String restoreComment(@PathVariable Long id, RedirectAttributes ra) {
        adminService.restoreComment(id);
        ra.addFlashAttribute("message", "댓글이 복구됐습니다.");
        return "redirect:/admin/comments";
    }

    @GetMapping("/regions")
    public String regions(Model model) {
        model.addAttribute("regions", adminService.getRegions());
        return "admin/regions";
    }

    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false) ReportStatus status,
            @RequestParam(required = false) ReportTargetType targetType,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {
        model.addAttribute("reports", adminService.getReports(status, targetType, pageable));
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedTargetType", targetType);
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("targetTypes", ReportTargetType.values());
        return "admin/reports";
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, RedirectAttributes ra) {
        adminService.resolveReport(id);
        ra.addFlashAttribute("message", "신고가 처리됐습니다.");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reports/{id}/dismiss")
    public String dismissReport(@PathVariable Long id, RedirectAttributes ra) {
        adminService.dismissReport(id);
        ra.addFlashAttribute("message", "신고가 기각됐습니다.");
        return "redirect:/admin/reports";
    }
}
