package com.model2.mvc.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;

public final class FileUploadUtil {

    // 필요시 프로젝트 정책에 맞춰 조절
    private static final long   MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_EXT =
    	    new HashSet<>(Arrays.asList("png","jpg","jpeg","gif","webp","svg","txt"));
    private FileUploadUtil() {}

    /** 업로드 베이스 경로(실 서버 경로) */
    public static String getUploadBasePath(HttpServletRequest request) {
        return getUploadBasePath(request.getServletContext());
    }

 // com.model2.mvc.common.util.FileUploadUtil

    public static String getUploadBasePath(ServletContext ctx) {
        String configured = ctx.getInitParameter("uploadBasePath");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured; // 외부 경로 사용!
        }
        // 최후의 보루: 과거 호환. 가능하면 쓰지 말 것.
        String fallback = ctx.getRealPath("/images/uploadFiles");
        return (fallback != null) ? fallback : System.getProperty("java.io.tmpdir");
    }

    /** yyyy/MM 하위 폴더 생성 (정리·충돌분산) */
    private static File ensureDatedDir(String basePath) {
        String yyyymm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        File dir = new File(basePath, yyyymm);
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** 파일 저장 → 저장된 상대경로(예: 2025/09/uuid_이름.png) 반환 */
    public static String save(MultipartFile file, HttpServletRequest request) throws IOException {
        return save(file, request.getServletContext());
    }

    public static String save(MultipartFile file, ServletContext ctx) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 용량 체크
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new IOException("파일이 너무 큽니다. 최대 " + (MAX_SIZE_BYTES/1024/1024) + "MB");
        }

        // 확장자 화이트리스트
        String original = file.getOriginalFilename();
        String ext = getExtension(original);
        if (!ext.isEmpty() && !ALLOWED_EXT.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new IOException("허용되지 않은 확장자: " + ext);
        }

        // 파일명 정리 + 충돌 방지
        String safeBase = sanitizeBaseName(stripExtension(original));
        String unique = UUID.randomUUID().toString();
        String filename = ext.isEmpty() ? (unique + "_" + safeBase)
                                        : (unique + "_" + safeBase + "." + ext);

        // 경로 구성
        String base = getUploadBasePath(ctx);
        File dir = ensureDatedDir(base);
        File dest = new File(dir, filename);

        // 저장
        file.transferTo(dest);

        // DB에는 베이스대비 상대경로만 저장 (ex: 2025/09/uuid_name.png)
        String relative = relativePath(base, dest.getAbsolutePath());
        return relative.replace(File.separatorChar, '/');
    }

    /** 기존 파일 삭제 (상대경로 기준) */
    public static void deleteIfExists(String relativePath, HttpServletRequest request) {
        if (relativePath == null || relativePath.trim().isEmpty()) return;
        File f = absoluteFile(relativePath, request.getServletContext());
        try {
            if (f.exists()) Files.delete(f.toPath());
        } catch (Exception ignore) {}
    }

    /** 상대경로 → 실제 파일 객체 */
    public static File absoluteFile(String relativePath, ServletContext ctx) {
        String base = getUploadBasePath(ctx);
        return new File(base, relativePath);
    }

    // ===== helpers =====
    private static String getExtension(String name) {
        if (name == null) return "";
        int idx = name.lastIndexOf('.');
        return (idx >= 0 && idx < name.length()-1) ? name.substring(idx+1) : "";
    }

    private static String stripExtension(String name) {
        if (name == null) return "file";
        int idx = name.lastIndexOf('.');
        return idx > 0 ? name.substring(0, idx) : name;
    }

    private static String sanitizeBaseName(String base) {
        if (base == null || base.trim().isEmpty()) base = "file";
        // 한글/라틴 등 정규화 후, 파일명에 부적합한 문자 제거
        String normalized = Normalizer.normalize(base, Normalizer.Form.NFKC);
        return normalized.replaceAll("[\\\\/:*?\"<>|\\s]+", "_");
    }

    private static String relativePath(String base, String absolute) {
        String b = new File(base).getAbsolutePath();
        String a = new File(absolute).getAbsolutePath();
        if (a.startsWith(b)) {
            String rel = a.substring(b.length());
            if (rel.startsWith(File.separator)) rel = rel.substring(1);
            return rel;
        }
        // base 추론 실패 시 파일명만 반환
        return new File(absolute).getName();
    }
}
