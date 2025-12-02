package org.example.manageFinances.src;
public class selectBankAccountDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public selectBankAccount findById(long id) {
        String sql = "SELECT * FROM bank_account WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{id},
                (rs, rowNum) -> new BankAccount(
                        rs.getLong("id"),
                        rs.getString("account_number"),
                        rs.getDouble("balance")

                )
        );
    }

    public void updateBalance(long id, double newBalance) {
        String sql = "UPDATE bank_account SET balance = ? WHERE id = ?";
        jdbcTemplate.update(sql, newBalance, id);
    }
    public interface BankAccountDAO {
        selectBankAccount findById(long id);
        selectBankAccount findByAccountNumber(String accountNumber);
        void updateBalance(long id, double newBalance);
        void (addExpselectAccount account);
        void create(selectBankAccount account);
        void delete(long id);
    }
    public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
        Optional<selectBankAccount> findByAccountNumber(String accountNumber);
    }

    // Fetch all accounts for a specific user
    public List<selectBankAccount> getAccountsForUser(int userId) {
        String sql = "SELECT * FROM bankAccount WHERE userId = ?";
        
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> {
            return new selectBankAccount(
                String.valueOf(rs.getInt("idbankAccount")),
                rs.getFloat("balance"),
                rs.getString("accountType"),
                rs.getFloat("otherIncome") // Map the new column
            );
        });
    }
}
