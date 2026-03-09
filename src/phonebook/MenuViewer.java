package phonebook;

import java.util.Scanner;

public class MenuViewer {
    private static final Scanner sc = new Scanner(System.in);

    public static int showMenu() {
        System.out.println("\n============================");
        System.out.println("   PHONE BOOK MANAGER v5");
        System.out.println("============================");
        System.out.println("1. 연락처 추가");
        System.out.println("2. 고급 검색");
        System.out.println("3. 연락처 수정");
        System.out.println("4. 연락처 삭제(휴지통)");
        System.out.println("5. 전체 보기");
        System.out.println("6. 정렬 보기(이름/그룹/즐겨찾기/생성순)");
        System.out.println("7. 즐겨찾기 토글");
        System.out.println("8. 휴지통 복구");
        System.out.println("9. 저장");
        System.out.println("10. 불러오기");
        System.out.println("11. 통계");
        System.out.println("12. GUI 실행(Swing)");
        System.out.println("13. 종료");
        System.out.print("선택 >> ");

        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String inputLine(String label) {
        System.out.print(label + ": ");
        return sc.nextLine().trim();
    }

    public static PhoneInfo inputPhoneInfo() {
        String name = inputLine("이름");
        String phone = inputLine("전화번호");
        String email = inputLine("이메일");
        String address = inputLine("주소");
        String birth = inputLine("생일(예:1999-01-31)");
        String group = inputLine("그룹(가족/친구/회사)");
        String memo = inputLine("메모");
        return new PhoneInfo(name, phone, email, address, memo, birth, group);
    }

    public static void pause() {
        System.out.print("(엔터를 누르면 계속) ");
        sc.nextLine();
    }
}
