package phonebook;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PhoneInfo {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String name;
    private String phoneNumber;
    private String email;
    private String address;
    private String memo;
    private String birth;
    private String group;
    private boolean favorite;
    private String createdAt;
    private String updatedAt;
    private String lastViewedAt;

    public PhoneInfo(String name, String phoneNumber, String email, String address, String memo, String birth, String group) {
        this(name, phoneNumber, email, address, memo, birth, group, false, now(), now(), "");
    }

    public PhoneInfo(String name, String phoneNumber, String email, String address, String memo, String birth, String group,
                     boolean favorite, String createdAt, String updatedAt, String lastViewedAt) {
        this.name = nz(name);
        this.phoneNumber = nz(phoneNumber);
        this.email = nz(email);
        this.address = nz(address);
        this.memo = nz(memo);
        this.birth = nz(birth);
        this.group = nz(group);
        this.favorite = favorite;
        this.createdAt = nz(createdAt).isBlank() ? now() : createdAt;
        this.updatedAt = nz(updatedAt).isBlank() ? now() : updatedAt;
        this.lastViewedAt = nz(lastViewedAt);
    }

    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getMemo() { return memo; }
    public String getBirth() { return birth; }
    public String getGroup() { return group; }
    public boolean isFavorite() { return favorite; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public String getLastViewedAt() { return lastViewedAt; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = nz(phoneNumber); touch(); }
    public void setEmail(String email) { this.email = nz(email); touch(); }
    public void setAddress(String address) { this.address = nz(address); touch(); }
    public void setMemo(String memo) { this.memo = nz(memo); touch(); }
    public void setBirth(String birth) { this.birth = nz(birth); touch(); }
    public void setGroup(String group) { this.group = nz(group); touch(); }
    public void setFavorite(boolean favorite) { this.favorite = favorite; touch(); }

    public void markViewed() { this.lastViewedAt = now(); }

    public void showPhoneInfo() {
        System.out.println("이름      : " + name + (favorite ? " ⭐" : ""));
        System.out.println("전화번호  : " + phoneNumber);
        System.out.println("이메일    : " + email);
        System.out.println("주소      : " + address);
        System.out.println("생일      : " + birth);
        System.out.println("그룹      : " + group);
        System.out.println("메모      : " + memo);
        System.out.println("생성일    : " + createdAt);
        System.out.println("수정일    : " + updatedAt);
        System.out.println("조회일    : " + lastViewedAt);
    }

    public String toCsv() {
        return escape(name) + "," + escape(phoneNumber) + "," + escape(email) + "," + escape(address) + ","
                + escape(memo) + "," + escape(birth) + "," + escape(group) + "," + escape(String.valueOf(favorite)) + ","
                + escape(createdAt) + "," + escape(updatedAt) + "," + escape(lastViewedAt);
    }

    public static PhoneInfo fromCsv(String line) {
        String[] f = parseCsv(line);
        return new PhoneInfo(
                get(f, 0), get(f, 1), get(f, 2), get(f, 3), get(f, 4), get(f, 5), get(f, 6),
                Boolean.parseBoolean(get(f, 7)), get(f, 8), get(f, 9), get(f, 10));
    }

    public String toJson() {
        return "{" +
                "\"name\":" + q(name) + "," +
                "\"phoneNumber\":" + q(phoneNumber) + "," +
                "\"email\":" + q(email) + "," +
                "\"address\":" + q(address) + "," +
                "\"memo\":" + q(memo) + "," +
                "\"birth\":" + q(birth) + "," +
                "\"group\":" + q(group) + "," +
                "\"favorite\":" + q(String.valueOf(favorite)) + "," +
                "\"createdAt\":" + q(createdAt) + "," +
                "\"updatedAt\":" + q(updatedAt) + "," +
                "\"lastViewedAt\":" + q(lastViewedAt) +
                "}";
    }

    private void touch() { this.updatedAt = now(); }

    private static String now() { return LocalDateTime.now().format(F); }

    private static String get(String[] arr, int idx) { return idx < arr.length ? arr[idx] : ""; }
    private static String nz(String s) { return s == null ? "" : s.trim(); }

    private static String escape(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }

    private static String q(String value) {
        String v = value == null ? "" : value;
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String[] parseCsv(String line) {
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuote = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuote && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuote = !inQuote;
                }
            } else if (c == ',' && !inQuote) {
                list.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        list.add(sb.toString());
        return list.toArray(new String[0]);
    }
}
