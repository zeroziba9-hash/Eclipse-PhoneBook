package phonebook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class PhoneBookSwingApp extends JFrame {
    private final PhoneBookManager manager;
    private final DefaultTableModel model;

    private final JTextField tfName = new JTextField();
    private final JTextField tfPhone = new JTextField();
    private final JTextField tfEmail = new JTextField();
    private final JTextField tfAddress = new JTextField();
    private final JTextField tfBirth = new JTextField();
    private final JTextField tfGroup = new JTextField();
    private final JTextField tfMemo = new JTextField();

    private final JTextField tfSearch = new JTextField();
    private final JComboBox<String> cbGroup = new JComboBox<>(new String[]{"ALL", "가족", "친구", "회사", "기타"});
    private final JComboBox<String> cbSort = new JComboBox<>(new String[]{"name", "group", "favorite", "created"});

    public PhoneBookSwingApp(PhoneBookManager manager) {
        this.manager = manager;
        setDarkLook();

        setTitle("PhoneBook GUI");
        setSize(1100, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel input = new JPanel(new GridLayout(7, 2, 6, 6));
        input.setBorder(BorderFactory.createTitledBorder("연락처 입력"));
        input.add(new JLabel("이름")); input.add(tfName);
        input.add(new JLabel("전화번호")); input.add(tfPhone);
        input.add(new JLabel("이메일")); input.add(tfEmail);
        input.add(new JLabel("주소")); input.add(tfAddress);
        input.add(new JLabel("생일")); input.add(tfBirth);
        input.add(new JLabel("그룹")); input.add(tfGroup);
        input.add(new JLabel("메모")); input.add(tfMemo);

        JPanel topActions = new JPanel(new GridLayout(2, 1, 6, 6));
        JPanel buttons1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("추가");
        JButton btnDelete = new JButton("삭제(이름)");
        JButton btnFav = new JButton("즐겨찾기 토글(이름)");
        JButton btnLoad = new JButton("불러오기");
        JButton btnSave = new JButton("저장");
        buttons1.add(btnAdd); buttons1.add(btnDelete); buttons1.add(btnFav); buttons1.add(btnLoad); buttons1.add(btnSave);

        JPanel buttons2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnSearch = new JButton("검색");
        JButton btnRefresh = new JButton("새로고침");
        buttons2.add(new JLabel("키워드")); buttons2.add(tfSearch); tfSearch.setColumns(16);
        buttons2.add(new JLabel("그룹")); buttons2.add(cbGroup);
        buttons2.add(new JLabel("정렬")); buttons2.add(cbSort);
        buttons2.add(btnSearch); buttons2.add(btnRefresh);

        topActions.add(buttons1);
        topActions.add(buttons2);

        model = new DefaultTableModel(new String[]{"이름", "전화", "이메일", "주소", "생일", "그룹", "메모", "즐겨찾기", "수정일"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(model);
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setBorder(BorderFactory.createTitledBorder("연락처 목록"));

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.add(input, BorderLayout.CENTER);
        left.add(topActions, BorderLayout.SOUTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, tablePane);
        split.setResizeWeight(0.42);
        add(split);

        btnAdd.addActionListener(e -> {
            PhoneInfo info = new PhoneInfo(tfName.getText(), tfPhone.getText(), tfEmail.getText(), tfAddress.getText(), tfMemo.getText(), tfBirth.getText(), tfGroup.getText());
            if (manager.add(info)) {
                manager.saveToFile();
                clearInput();
                refreshTable();
                alert("추가 완료");
            } else {
                alert("추가 실패: 중복/형식 오류");
            }
        });

        btnDelete.addActionListener(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { alert("삭제할 이름 입력"); return; }
            if (manager.deleteByName(name)) {
                manager.saveToFile();
                refreshTable();
                alert("삭제 완료(휴지통 이동)");
            } else {
                alert("해당 이름 없음");
            }
        });

        btnFav.addActionListener(e -> {
            String name = tfName.getText().trim();
            if (name.isEmpty()) { alert("이름 입력"); return; }
            PhoneInfo p = null;
            for (PhoneInfo x : manager.getAll()) if (x.getName().equalsIgnoreCase(name)) p = x;
            if (p == null) { alert("해당 이름 없음"); return; }
            p.setFavorite(!p.isFavorite());
            manager.saveToFile();
            refreshTable();
        });

        btnLoad.addActionListener(e -> { manager.loadFromFile(); refreshTable(); alert("불러오기 완료"); });
        btnSave.addActionListener(e -> { manager.saveToFile(); alert("저장 완료"); });
        btnSearch.addActionListener(e -> searchAndPaint());
        btnRefresh.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private void searchAndPaint() {
        String keyword = tfSearch.getText();
        String group = (String) cbGroup.getSelectedItem();
        String sort = (String) cbSort.getSelectedItem();

        List<PhoneInfo> all = manager.advancedSearch(keyword, group);
        applySort(all, sort);

        model.setRowCount(0);
        for (PhoneInfo p : all) addRow(p);
    }

    private void refreshTable() {
        List<PhoneInfo> all = manager.getAll();
        applySort(all, (String) cbSort.getSelectedItem());

        model.setRowCount(0);
        for (PhoneInfo p : all) addRow(p);
    }

    private void applySort(List<PhoneInfo> all, String sort) {
        if (sort == null) sort = "name";
        switch (sort) {
            case "group":
                all.sort(Comparator.comparing(PhoneInfo::getGroup, String.CASE_INSENSITIVE_ORDER).thenComparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "favorite":
                all.sort(Comparator.comparing(PhoneInfo::isFavorite).reversed().thenComparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "created":
                all.sort(Comparator.comparing(PhoneInfo::getCreatedAt));
                break;
            default:
                all.sort(Comparator.comparing(PhoneInfo::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    private void addRow(PhoneInfo p) {
        model.addRow(new Object[]{p.getName(), p.getPhoneNumber(), p.getEmail(), p.getAddress(), p.getBirth(), p.getGroup(), p.getMemo(), p.isFavorite() ? "⭐" : "", p.getUpdatedAt()});
    }

    private void clearInput() {
        tfName.setText(""); tfPhone.setText(""); tfEmail.setText(""); tfAddress.setText("");
        tfBirth.setText(""); tfGroup.setText(""); tfMemo.setText("");
    }

    private void alert(String msg) { JOptionPane.showMessageDialog(this, msg); }

    private void setDarkLook() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
    }

    public static void launch(PhoneBookManager manager) {
        SwingUtilities.invokeLater(() -> new PhoneBookSwingApp(manager).setVisible(true));
    }
}
