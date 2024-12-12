import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

class User {
    protected String username;
    protected String password;
    protected String captcha;

    public User(String username, String password, String captcha) {
        this.username = username;
        this.password = password;
        this.captcha = captcha;
    }

    public boolean validateLogin() {
        return username.equals("fatih athaya") && password.equals("0612") && captcha.equals("l4n1");
    }
}

class Kasir extends User {
    private String namaKasir;

    public Kasir(String username, String password, String captcha, String namaKasir) {
        super(username, password, captcha);
        this.namaKasir = namaKasir;
    }

    public String getNamaKasir() {
        return namaKasir;
    }
}

class InvalidInputException extends Exception {
    public InvalidInputException(String message) {
        super(message);
    }
}

public class Toko {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/JavaMartDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Sesuaikan dengan password MySQL Anda

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("+-----------------------------------------------------+");
            System.out.print("Username : ");
            String username = scanner.nextLine();
            System.out.print("Password : ");
            String password = scanner.nextLine();
            System.out.print("Captcha  : ");
            String captcha = scanner.nextLine();

            Kasir kasir = new Kasir(username, password, captcha, "UDIN");

            if (!kasir.validateLogin()) {
                System.out.println("Login gagal, silakan diulangi.");
                return;
            }

            boolean running = true;

            while (running) {
                System.out.println("\n+-----------------------------------------------------+");
                System.out.println("Pilih Tindakan:");
                System.out.println("1. Tambah Transaksi");
                System.out.println("2. Lihat Semua Transaksi");
                System.out.println("3. Perbarui Transaksi");
                System.out.println("4. Hapus Transaksi");
                System.out.println("5. Keluar");
                System.out.print("Pilihan Anda: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear buffer

                switch (choice) {
                    case 1:
                        tambahTransaksi(scanner, connection, kasir);
                        break;
                    case 2:
                        displayAllTransactions(connection);
                        break;
                    case 3:
                        perbaruiTransaksi(scanner, connection);
                        break;
                    case 4:
                        hapusTransaksi(scanner, connection);
                        break;
                    case 5:
                        running = false;
                        System.out.println("Keluar dari aplikasi.");
                        break;
                    default:
                        System.out.println("Pilihan tidak valid, silakan coba lagi.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Terjadi kesalahan: " + e.getMessage());
        }
    }

    private static void tambahTransaksi(Scanner scanner, Connection connection, Kasir kasir) throws SQLException, InvalidInputException {
        System.out.println("+-----------------------------------------------------+");
        System.out.print("No. Faktur      : ");
        String noFaktur = scanner.nextLine();
        System.out.print("Kode Barang     : ");
        String kodeBarang = scanner.nextLine();
        System.out.print("Nama Barang     : ");
        String namaBarang = scanner.nextLine();
        System.out.print("Harga Barang    : ");
        double hargaBarang = scanner.nextDouble();
        if (hargaBarang <= 0) {
            throw new InvalidInputException("Harga barang tidak valid!");
        }
        System.out.print("Jumlah Beli     : ");
        int jumlahBeli = scanner.nextInt();
        if (jumlahBeli <= 0) {
            throw new InvalidInputException("Jumlah beli tidak valid!");
        }
        scanner.nextLine(); // Clear buffer

        double totalHarga = hargaBarang * jumlahBeli;
        String tanggalWaktu = getCurrentDateTime();

        String insertQuery = "INSERT INTO Transaksi (noFaktur, kodeBarang, namaBarang, hargaBarang, jumlahBeli, totalHarga, namaKasir, tanggalWaktu) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, noFaktur);
            preparedStatement.setString(2, kodeBarang);
            preparedStatement.setString(3, namaBarang);
            preparedStatement.setDouble(4, hargaBarang);
            preparedStatement.setInt(5, jumlahBeli);
            preparedStatement.setDouble(6, totalHarga);
            preparedStatement.setString(7, kasir.getNamaKasir());
            preparedStatement.setString(8, tanggalWaktu);

            preparedStatement.executeUpdate();
            System.out.println("Transaksi berhasil disimpan ke database.");
        }
    }

    private static void displayAllTransactions(Connection connection) throws SQLException {
        String selectQuery = "SELECT * FROM Transaksi";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {

            System.out.println("+-----------------------------------------------------+");
            while (resultSet.next()) {
                System.out.println("No. Faktur  : " + resultSet.getString("noFaktur"));
                System.out.println("Kode Barang : " + resultSet.getString("kodeBarang"));
                System.out.println("Nama Barang : " + resultSet.getString("namaBarang"));
                System.out.println("Harga Barang: " + resultSet.getDouble("hargaBarang"));
                System.out.println("Jumlah Beli : " + resultSet.getInt("jumlahBeli"));
                System.out.println("Total Harga : " + resultSet.getDouble("totalHarga"));
                System.out.println("Nama Kasir  : " + resultSet.getString("namaKasir"));
                System.out.println("Tanggal     : " + resultSet.getString("tanggalWaktu"));
                System.out.println("+-----------------------------------------------------+");
            }
        }
    }

    private static void perbaruiTransaksi(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Masukkan No. Faktur yang ingin diupdate: ");
        String updateFaktur = scanner.nextLine();

        String updateQuery = "UPDATE Transaksi SET namaBarang = ?, hargaBarang = ?, jumlahBeli = ?, totalHarga = ? WHERE noFaktur = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
            System.out.print("Nama Barang Baru: ");
            String newNamaBarang = scanner.nextLine();
            System.out.print("Harga Barang Baru: ");
            double newHargaBarang = scanner.nextDouble();
            System.out.print("Jumlah Beli Baru: ");
            int newJumlahBeli = scanner.nextInt();
            scanner.nextLine(); // Clear buffer

            double newTotalHarga = newHargaBarang * newJumlahBeli;

            preparedStatement.setString(1, newNamaBarang);
            preparedStatement.setDouble(2, newHargaBarang);
            preparedStatement.setInt(3, newJumlahBeli);
            preparedStatement.setDouble(4, newTotalHarga);
            preparedStatement.setString(5, updateFaktur);

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Data berhasil diperbarui.");
            } else {
                System.out.println("No. Faktur tidak ditemukan.");
            }
        }
    }

    private static void hapusTransaksi(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Masukkan No. Faktur yang ingin dihapus: ");
        String deleteFaktur = scanner.nextLine();

        String deleteQuery = "DELETE FROM Transaksi WHERE noFaktur = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setString(1, deleteFaktur);

            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Data berhasil dihapus.");
            } else {
                System.out.println("No. Faktur tidak ditemukan.");
            }
        }
    }

    private static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }
}