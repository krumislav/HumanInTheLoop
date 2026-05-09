package mk.ukim.finki.humanintheloopllm.controller;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.service.ScientificPaperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/papers")
@RequiredArgsConstructor
public class ScientificPaperController {

    private final ScientificPaperService paperService;

    @GetMapping
    public String papersPage(Model model) {
        model.addAttribute("papers", paperService.findAll());
        return "papers";
    }

    @PostMapping("/upload")
    public String uploadPaper(@RequestParam("file") MultipartFile file,
                              @RequestParam("title") String title,
                              @RequestParam("author") String author,
                              @RequestParam("year") Integer year) {

        if (file.isEmpty()) {
            return "redirect:/papers?error=nofile";
        }

        if (!file.getOriginalFilename().endsWith(".pdf")) {
            return "redirect:/papers?error=notpdf";
        }

        paperService.uploadPaper(file, title, author, year);
        return "redirect:/papers?success=true";
    }

    @PostMapping("/delete/{id}")
    public String deletePaper(@PathVariable Long id) {
        paperService.deletePaper(id);
        return "redirect:/papers?deleted=true";
    }

    @PostMapping("/edit/{id}")
    public String editPaper(@PathVariable Long id,
                            @RequestParam String title,
                            @RequestParam String author,
                            @RequestParam Integer year) {
        paperService.updatePaper(id, title, author, year);
        return "redirect:/papers?updated=true";
    }
}