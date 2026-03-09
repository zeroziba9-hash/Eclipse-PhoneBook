package phonebook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PhoneBookManager {
    private static final int MAX_SIZE = 1000;
    private static final Path SAVE_FILE = Paths.get("phonebook-data.csv");
    private static final Path TRASH_FILE = Paths.get("phonebook-trash.csv");
    private static final Path JSON_FILE = Paths.get("phonebook-data.json");

    private final List<PhoneInfo> infoStorage;
    private final List<PhoneInfo> trash;

    public PhoneBookManager() {
        this.infoStorage = new ArrayList<>();
        this.trash = new ArrayList<>();
    }

    public List<PhoneInfo> getAll() { return new ArrayList<>(infoStorage); }

    public boolean add(PhoneInfo info) {
        info.setPhoneNumber(autoFormatPhone(info.getPhoneNumber()));
        info.setBirth(autoFormatBirth(info.getBirth()));

        if (infoStorage.size() >= MAX_SIZE) return false;
        if (info.getName().isBlank()) return false;
        if (!isValidPhone(info.getPhoneNumber())) return false;
        if (!isValidEmail(info.getEmail())) return false;
        if (!isValidBirth(info.getBirth())) return false;
        if (!isUniqueName(info.getName())) return false;
        if (!isUniquePhone(info.getPhoneNumber())) return false;

        infoStorage.add(info);
        return true;
    }

    public void inputData() {
        while (true) {
            String name;
            while (true) {
                name = MenuViewer.inputLine("이름");
                if (!name.isBlank()) break;
                System.out.println("잘못된 입력입니다. 이름은 비울 수 없습니다.");
            }

            String phone;
            while (true) {
                phone = autoFormatPhone(MenuViewer.inputLine("전화번호"));
                if (isValidPhone(phone)) break;
                System.out.println("잘못된 입력입니다. 전화번호를 다시 입력하세요. (예: 01012341234)");
            }

            String email;
            while (true) {
                email = MenuViewer.inputLine("이메일");
                if (isValidEmail(email)) break;
                System.out.println("잘못된 입력입니다. 이메일 형식을 확인하세요.");
            }

            String address = MenuViewer.inputLine("주소");

            String birth;
            while (true) {
                birth = autoFormatBirth(MenuViewer.inputLine("생년월일(예: 1995-01-01 또는 19950101)"));
                if (isValidBirth(birth)) break;
                System.out.println("잘못된 입력입니다. 생년월일을 다시 입력하세요.");
            }

            String group = MenuViewer.inputLine("그룹(가족/친구/회사)");
            String memo = MenuViewer.inputLine("메모");

            PhoneInfo info = new PhoneInfo(name, phone, email, address, memo, birth, group);

            if (!isUniqueName(info.getName())) {
                System.out.println("잘못된 입력입니다. 같은 이름이 이미 있습니다.");
            } else if (!isUniquePhone(info.getPhoneNumber())) {
                System.out.println("잘못된 입력입니다. 같은 전화번호가 이미 있습니다.");
            } else if (add(info)) {
                System.out.println("연락처 추가 완료.");
                return;
            } else {
                System.out.println("잘못된 입력입니다. (중복/형식 오류)");
            }

            String retry = MenuViewer.inputLine("다시 입력할까요? (Y/N)");
            if (!retry.equalsIgnoreCase("y")) return;
        }
    }

    public void searchData() {
        String keyword = MenuViewer.inputLine("검색 키워드(이름/번호/이메일/그룹)");
        if (keyword.isBlank()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        List<PhoneInfo> found = advancedSearch(keyword, "ALL");
        if (found.isEmpty()) {
            System.out.println("검색 결과가 없습니다.");
            return;
        }

        for (PhoneInfo info : found) {
            info.markViewed();
            System.out.println("-------------------------");
            info.showPhoneInfo();
        }
        System.out.println("총 " + found.size() + "건 찾았습니다.");
    }

    public List<PhoneInfo> advancedSearch(String keyword, String groupFilter) {
        String k = keyword == null ? "" : keyword.trim().toLowerCase();
        String g = groupFilter == null ? "ALL" : groupFilter.trim().toLowerCase();
        List<PhoneInfo> out = new ArrayList<>();

        for (PhoneInfo info : infoStorage) {
            boolean groupOk = g.equals("all") || info.getGroup().toLowerCase().contains(g);
            boolean keywordOk = k.isBlank()
                    || info.getName().toLowerCase().contains(k)
                    || normalizePhone(info.getPhoneNumber()).contains(normalizePhone(k))
                    || info.getEmail().toLowerCase().contains(k)
                    || info.getGroup().toLowerCase().contains(k)
                    || info.getAddress().toLowerCase().contains(k)
                    || info.getMemo().toLowerCase().contains(k);

            if (groupOk && keywordOk) out.add(info);
        }
        return out;
    }

    public void updateData() {
        String name = MenuViewer.inputLine("수정할 정확한 이름");
        PhoneInfo target = findByExactName(name);
        if (target == null) {
            System.out.println("해당 이름의 연락처가 없습니다.");
            return;
        }

        System.out.println("수정할 값만 입력하세요. (유지하려면 엔터)");
        String newPhone = MenuViewer.inputLine("새 전화번호");
        String newEmail = MenuViewer.inputLine("새 이메일");
        String newAddress = MenuViewer.inputLine("새 주소");
        String newBirth = MenuViewer.inputLine("새 생일");
        String newGroup = MenuViewer.inputLine("새 그룹");
        String newMemo = MenuViewer.inputLine("새 메모");
        String fav = MenuViewer.inputLine("즐겨찾기(Y/N)");

        if (!newPhone.isBlank()) {
            newPhone = autoFormatPhone(newPhone);
            if (!isValidPhone(newPhone) || !isUniquePhoneForUpdate(target, newPhone)) {
                System.out.println("전화번호 형식/중복 오류로 수정 취소.");
                return;
            }
            target.setPhoneNumber(newPhone);
        }
        if (!newEmail.isBlank() && isValidEmail(newEmail)) target.setEmail(newEmail);
        if (!newAddress.isBlank()) target.setAddress(newAddress);
        if (!newBirth.isBlank()) {
            newBirth = autoFormatBirth(newBirth);
            if (!isValidBirth(newBirth)) {
                System.out.println("생년월일 형식 오류로 수정 취소. (예: 1995-01-01 또는 19950101)");
                return;
            }
            target.setBirth(newBirth);
        }
        if (!newGroup.isBlank()) target.setGroup(newGroup);
        if (!newMemo.isBlank()) target.setMemo(newMemo);
        if (fav.equalsIgnoreCase("y") || fav.equalsIgnoreCase("n")) target.setFavorite(fav.equalsIgnoreCase("y"));

        System.out.println("수정 완료.");
    }

    public void deleteData() {
        String name = MenuViewer.inputLine("삭제할 정확한 이름");
        if (deleteByName(name)) {
            System.out.println("삭제 완료(휴지통 이동).");
        } else {
            System.out.println("해당 이름의 연락처가 없습니다.");
        }
    }

    public boolean deleteByName(String name) {
        for (int i = 0; i < infoStorage.size(); i++) {
            if (infoStorage.get(i).getName().equalsIgnoreCase(name)) {
                trash.add(infoStorage.remove(i));
                return true;
            }
        }
        return false;
    }

    public void restoreFromTrash() {
        if (trash.isEmpty()) {
            System.out.println("휴지통이 비어 있습니다.");
            return;
        }
        for (int i = 0; i < trash.size(); i++) {
            PhoneInfo p = trash.get(i);
            System.out.println("[" + (i + 1) + "] " + p.getName() + " / " + p.getPhoneNumber());
        }
        String idxRaw = MenuViewer.inputLine("복구할 번호(0 취소)");
        int idx;
        try { idx = Integer.parseInt(idxRaw); } catch (Exception e) { idx = -1; }
        if (idx <= 0 || idx > trash.size()) return;

        PhoneInfo target = trash.remove(idx - 1);
        if (isUniqueName(target.getName()) && isUniquePhone(target.getPhoneNumber())) {
            infoStorage.add(target);
            System.out.println("복구 완료.");
        } else {
            System.out.println("중복으로 인해 복구 실패.");
            trash.add(target);
        }
    }

    public void toggleFavorite() {
        String name = MenuViewer.inputLine("즐겨찾기 토글할 이름");
        PhoneInfo p = findByExactName(name);
        if (p == null) {
            System.out.println("해당 이름 없음");
            return;
        }
        p.setFavorite(!p.isFavorite());
        System.out.println("변경 완료: " + (p.isFavorite() ? "즐겨찾기 ON" : "즐겨찾기 OFF"));
    }

    public void showAllData() {
        showSorted("name");
    }

    public void showSorted(String sortKey) {
        if (infoStorage.isEmpty()) {
            System.out.println("저장된 데이터가 없습니다.");
            return;
        }

        List<PhoneInfo> copied = getAll();
        Comparator<PhoneInfo> c;

        switch (sortKey.toLowerCase()) {
            case "created":
                c = Comparator.comparing(PhoneInfo::getCreatedAt);
                break;
            case "group":
                c = Comparator.comparing(PhoneInfo::getGroup, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "favorite":
                c = Comparator.comparing(PhoneInfo::isFavorite).reversed()
                        .thenComparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            default:
                c = Comparator.comparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER);
        }

        copied.sort(c);

        System.out.println("\n===== 전체 연락처 =====");
        for (int i = 0; i < copied.size(); i++) {
            System.out.println("[" + (i + 1) + "]");
            copied.get(i).showPhoneInfo();
            System.out.println();
        }
        System.out.println("총 " + copied.size() + "명");
    }

    public void showStats() {
        int family = 0, friend = 0, company = 0, etc = 0;
        for (PhoneInfo p : infoStorage) {
            String g = p.getGroup().toLowerCase();
            if (g.contains("가족")) family++;
            else if (g.contains("친구")) friend++;
            else if (g.contains("회사")) company++;
            else etc++;
        }
        System.out.println("총 인원: " + infoStorage.size());
        System.out.println("가족: " + family + ", 친구: " + friend + ", 회사: " + company + ", 기타: " + etc);
    }

    public void saveToFile() {
        try {
            backupCurrent();
            List<String> lines = new ArrayList<>();
            for (PhoneInfo info : infoStorage) lines.add(info.toCsv());
            Files.write(SAVE_FILE, lines, StandardCharsets.UTF_8);

            List<String> t = new ArrayList<>();
            for (PhoneInfo info : trash) t.add(info.toCsv());
            Files.write(TRASH_FILE, t, StandardCharsets.UTF_8);

            exportJson();
            System.out.println("저장 완료: " + SAVE_FILE.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("저장 실패: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        loadListFromCsv(SAVE_FILE, infoStorage);
        loadListFromCsv(TRASH_FILE, trash);
        System.out.println("불러오기 완료. 연락처 " + infoStorage.size() + "명 / 휴지통 " + trash.size() + "명");
    }

    private void loadListFromCsv(Path path, List<PhoneInfo> target) {
        if (!Files.exists(path)) return;
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            target.clear();
            for (String line : lines) {
                if (line.isBlank()) continue;
                PhoneInfo info = PhoneInfo.fromCsv(line);
                if (!info.getName().isBlank() && isValidPhone(info.getPhoneNumber())) target.add(info);
            }
        } catch (IOException ignored) {
        }
    }

    public void exportJson() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("[\n");
            for (int i = 0; i < infoStorage.size(); i++) {
                sb.append("  ").append(infoStorage.get(i).toJson());
                if (i < infoStorage.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append("]\n");
            Files.writeString(JSON_FILE, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    private void backupCurrent() {
        try {
            if (!Files.exists(SAVE_FILE)) return;
            Path backupDir = Paths.get("backup");
            Files.createDirectories(backupDir);
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            Path backupFile = backupDir.resolve("phonebook-" + ts + ".csv");
            Files.copy(SAVE_FILE, backupFile);
        } catch (IOException ignored) {
        }
    }

    public String autoFormatPhone(String phone) {
        String n = normalizePhone(phone);
        if (n.length() == 11 && n.startsWith("010")) {
            return n.substring(0, 3) + "-" + n.substring(3, 7) + "-" + n.substring(7);
        }
        if (n.length() == 10 && n.startsWith("02")) {
            return n.substring(0, 2) + "-" + n.substring(2, 6) + "-" + n.substring(6);
        }
        return phone == null ? "" : phone.trim();
    }

    public String autoFormatBirth(String birth) {
        if (birth == null) return "";
        String b = birth.trim();
        if (b.matches("\\d{8}")) {
            return b.substring(0, 4) + "-" + b.substring(4, 6) + "-" + b.substring(6, 8);
        }
        return b;
    }

    private PhoneInfo findByExactName(String name) {
        for (PhoneInfo info : infoStorage) if (info.getName().equalsIgnoreCase(name)) return info;
        return null;
    }

    private boolean isUniqueName(String name) { return findByExactName(name) == null; }

    private boolean isUniquePhone(String phone) {
        String normalized = normalizePhone(phone);
        for (PhoneInfo info : infoStorage) {
            if (normalizePhone(info.getPhoneNumber()).equals(normalized)) return false;
        }
        return true;
    }

    private boolean isUniquePhoneForUpdate(PhoneInfo target, String newPhone) {
        String normalized = normalizePhone(newPhone);
        for (PhoneInfo info : infoStorage) {
            if (info == target) continue;
            if (normalizePhone(info.getPhoneNumber()).equals(normalized)) return false;
        }
        return true;
    }

    private boolean isValidPhone(String phone) {
        return phone != null && normalizePhone(phone).matches("[0-9]{8,11}");
    }

    private boolean isValidEmail(String email) {
        return email == null || email.isBlank() || email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidBirth(String birth) {
        return birth == null || birth.isBlank() || birth.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }
}
