package phonebook;

public class PhoneBookApp {
    public static void main(String[] args) {
        PhoneBookManager manager = new PhoneBookManager();
        manager.loadFromFile();

        while (true) {
            int choice = MenuViewer.showMenu();

            switch (choice) {
                case 1:
                    manager.inputData();
                    MenuViewer.pause();
                    break;
                case 2:
                    manager.searchData();
                    MenuViewer.pause();
                    break;
                case 3:
                    manager.updateData();
                    MenuViewer.pause();
                    break;
                case 4:
                    manager.deleteData();
                    MenuViewer.pause();
                    break;
                case 5:
                    manager.showAllData();
                    MenuViewer.pause();
                    break;
                case 6:
                    String sortKey = MenuViewer.inputLine("정렬키(name/group/favorite/created)");
                    manager.showSorted(sortKey);
                    MenuViewer.pause();
                    break;
                case 7:
                    manager.toggleFavorite();
                    MenuViewer.pause();
                    break;
                case 8:
                    manager.restoreFromTrash();
                    MenuViewer.pause();
                    break;
                case 9:
                    manager.saveToFile();
                    MenuViewer.pause();
                    break;
                case 10:
                    manager.loadFromFile();
                    MenuViewer.pause();
                    break;
                case 11:
                    manager.showStats();
                    MenuViewer.pause();
                    break;
                case 12:
                    PhoneBookSwingApp.launch(manager);
                    System.out.println("GUI 창을 열었습니다.");
                    MenuViewer.pause();
                    break;
                case 13:
                    manager.saveToFile();
                    System.out.println("프로그램 종료.");
                    return;
                default:
                    System.out.println("잘못된 선택입니다.");
                    MenuViewer.pause();
            }
        }
    }
}
