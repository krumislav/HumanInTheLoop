package mk.ukim.finki.humanintheloopllm.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.humanintheloopllm.model.PaperChunk;
import mk.ukim.finki.humanintheloopllm.model.ScientificPaper;
import mk.ukim.finki.humanintheloopllm.repository.PaperChunkRepository;
import mk.ukim.finki.humanintheloopllm.repository.ScientificPaperRepository;
import mk.ukim.finki.humanintheloopllm.service.ScientificPaperService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.Loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScientificPaperServiceImpl implements ScientificPaperService {

    private final ScientificPaperRepository paperRepository;
    private final PaperChunkRepository chunkRepository;

    @Override
    public ScientificPaper uploadPaper(MultipartFile file, String title, String author, Integer year) {

        // 1. Save the paper info to the database first
        ScientificPaper paper = new ScientificPaper();
        paper.setTitle(title);
        paper.setAuthor(author);
        paper.setYear(year);
        paper.setFileName(file.getOriginalFilename());
        ScientificPaper savedPaper = paperRepository.save(paper);

        // 2. Extract text from the PDF using PDFBox
        try {
            PDDocument document = Loader.loadPDF(file.getBytes());
            PDFTextStripper stripper = new PDFTextStripper();
            String fullText = stripper.getText(document);
            document.close();

            // 3. Split the text into chunks and save each one
            List<PaperChunk> chunks = splitIntoChunks(fullText, savedPaper);
            chunkRepository.saveAll(chunks);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + e.getMessage());
        }

        return savedPaper;
    }

    @Override
    public List<ScientificPaper> findAll() {
        return paperRepository.findAll();
    }

    @Override
    public List<PaperChunk> searchChunks(String query) {
        List<String> keywords = extractKeywords(query);

        // Search for each keyword and collect all results
        List<PaperChunk> results = new ArrayList<>();

        for (String keyword : keywords) {
            List<PaperChunk> found = chunkRepository.findByContentContainingIgnoreCase(keyword);
            for (PaperChunk chunk : found) {
                // Avoid adding the same chunk twice
                if (!results.contains(chunk)) {
                    results.add(chunk);
                }
            }
        }

        // Sort by relevance — chunks that contain MORE keywords come first
        results.sort((a, b) -> {
            int scoreB = countKeywordMatches(b.getContent(), keywords);
            int scoreA = countKeywordMatches(a.getContent(), keywords);
            return Integer.compare(scoreB, scoreA);
        });

        // Return max 5 most relevant chunks so the AI prompt doesn't get too long
        return results.stream().limit(5).toList();
    }

    @Override
    public void deletePaper(Long id) {
        paperRepository.deleteById(id);
    }

    @Override
    public void updatePaper(Long id, String title, String author, Integer year) {
        ScientificPaper paper = paperRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paper not found"));
        paper.setTitle(title);
        paper.setAuthor(author);
        paper.setYear(year);
        paperRepository.save(paper);
    }

    // ── Helpers ──────────────────────────────────────────

    private List<PaperChunk> splitIntoChunks(String text, ScientificPaper paper) {
        List<PaperChunk> chunks = new ArrayList<>();

        int chunkSize = 500;  // 500 карактери по chunk
        int chunkIndex = 0;

        System.out.println("========== PDF CHUNKING DEBUG ==========");
        System.out.println("Total text length: " + text.length());

        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            String chunkContent = text.substring(i, end).trim();

            // Прескокни многу кратки chunks
            if (chunkContent.length() < 50) {
                continue;
            }

            PaperChunk chunk = new PaperChunk();
            chunk.setContent(chunkContent);
            chunk.setChunkIndex(chunkIndex);
            chunk.setPageNumber(0);
            chunk.setPaper(paper);
            chunks.add(chunk);

            chunkIndex++;
        }

        System.out.println("Created " + chunks.size() + " chunks");
        System.out.println("========================================");

        return chunks;
    }
    private List<String> extractKeywords(String query) {
        String[] stopWords = {
            "what", "is", "are", "the", "a", "an", "how", "why", "when",
            "where", "who", "does", "do", "in", "of", "to", "and", "or",
            "for", "with", "about", "that", "this", "was", "has", "have",
            "been", "can", "could", "would", "should", "their", "there",
            "which", "from", "they", "them", "some", "any", "its"
        };

        String[] words = query.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");

        List<String> keywords = new ArrayList<>();
        for (String word : words) {
            // Skip stop words and very short words
            if (word.length() < 4) continue;
            if (Arrays.asList(stopWords).contains(word)) continue;
            if (!keywords.contains(word)) {
                keywords.add(word);
            }
        }

        return keywords;
    }

    private int countKeywordMatches(String content, List<String> keywords) {
        String lowerContent = content.toLowerCase();
        int count = 0;
        for (String keyword : keywords) {
            if (lowerContent.contains(keyword)) {
                count++;
            }
        }
        return count;
    }
}