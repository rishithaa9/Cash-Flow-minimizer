import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

public class Project_CashFlow_Minimizer extends JFrame {
    private JButton addTransactionButton, minimizeButton, importButton, exportButton, clearButton;
    private JTextPane transactionInputPane, minimizedTransactionPane;
    private JTextField payerField, payeeField, amountField;
    private JComboBox<String> currencyComboBox;

    private List<Transaction> transactions;
    private List<Transaction> minTransac;
    private static Set<String> currenciesEncountered;

    public Project_CashFlow_Minimizer() {
        super("Cash Flow Minimizer");
        transactions = new ArrayList<>();
        currenciesEncountered = new HashSet<>(); 

        initializeComponents();

        setSize(700, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeComponents() {
        JPanel inputPanel = new JPanel(new GridLayout(5, 4, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel payerLabel = new JLabel("Payer: ");
        payerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(payerLabel);
        payerField = new JTextField();
        inputPanel.add(payerField);

        JLabel payeeLabel = new JLabel("Payee: ");
        payeeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(payeeLabel);
        payeeField = new JTextField();
        inputPanel.add(payeeField);

        JLabel amountLabel = new JLabel("Amount: ");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(amountLabel);
        amountField = new JTextField();
        inputPanel.add(amountField);

        JLabel currencyLabel = new JLabel("Currency: ");
        currencyLabel.setFont(new Font("Arial", Font.BOLD, 14));
        inputPanel.add(currencyLabel);
        String[] currencies = {"Select", "USD", "INR", "EUR", "GBP", "CAD", "JPY"};
        currencyComboBox = new JComboBox<>(currencies);
        currencyComboBox.setSelectedIndex(0);
        inputPanel.add(currencyComboBox);

        addTransactionButton = new JButton("Add Transaction");
        addTransactionButton.setFont(new Font("Arial", Font.BOLD, 14));
        addTransactionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTransaction();
            }
        });
        inputPanel.add(addTransactionButton);

        minimizeButton = new JButton("Minimize Cash Flow");
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 14));
        minimizeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                minimizeFlow();
            }
        });
        inputPanel.add(minimizeButton);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(230, 244, 250));

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel transactionPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        transactionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        transactionInputPane = new JTextPane();
        transactionInputPane.setFont(new Font("Arial", Font.PLAIN, 14));
        transactionInputPane.setEditable(false);
        JScrollPane inputScrollPane = new JScrollPane(transactionInputPane);
        transactionPanel.add(inputScrollPane);

        minimizedTransactionPane = new JTextPane();
        minimizedTransactionPane.setFont(new Font("Arial", Font.PLAIN, 14));
        minimizedTransactionPane.setEditable(false);
        JScrollPane minimizedScrollPane = new JScrollPane(minimizedTransactionPane);
        transactionPanel.add(minimizedScrollPane);

        mainPanel.add(transactionPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        importButton = new JButton("Import");
        importButton.setFont(new Font("Arial", Font.BOLD, 12));
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importCSV();
            }
        });
        buttonPanel.add(importButton);

        exportButton = new JButton("Export");
        exportButton.setFont(new Font("Arial", Font.BOLD, 12));
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportCSV();
            }
        });
        buttonPanel.add(exportButton);

        clearButton = new JButton("Clear Transactions");
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearTransactions();
            }
        });
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }    

    private void addTransaction() {
        String payer = payerField.getText();
        String payee = payeeField.getText();
        String amountText = amountField.getText();
        String currency = (String) currencyComboBox.getSelectedItem();

        if (payer.isEmpty() || payee.isEmpty() || amountText.isEmpty() || currency.equals("Select")) {
            StringBuilder errorMessage = new StringBuilder("Please fill the following fields:\n");
            if (payer.isEmpty()) {
                errorMessage.append("-> Payer\n");
            }
            if (payee.isEmpty()) {
                errorMessage.append("-> Payee\n");
            }
            if (amountText.isEmpty()) {
                errorMessage.append("-> Amount\n");
            }
            if (currency.equals("Select")) {
                errorMessage.append("-> Currency\n");
            }

            JOptionPane.showMessageDialog(this, errorMessage.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Transaction transaction = new Transaction(payer, payee, amount, currency);
        transactions.add(transaction);
        currenciesEncountered.add(currency);

        StyledDocument doc = transactionInputPane.getStyledDocument();
        Style regularStyle = doc.addStyle("regular", null);
        StyleConstants.setFontFamily(regularStyle, "Arial");

        try {
            doc.insertString(doc.getLength(), payer + " pays ", regularStyle);
            doc.insertString(doc.getLength(), String.format("%.2f", amount), regularStyle);
            doc.insertString(doc.getLength(), " " + currency + " to " + payee + "\n", regularStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        payerField.setText("");
        payeeField.setText("");
        currencyComboBox.setSelectedIndex(0);
        amountField.setText("");
    }
    
    private void minimizeFlow() {
        if (transactions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions added", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<String> participants = getParticipants();
        int numParticipants = participants.size();
        Graph graph = new Graph(numParticipants);
        for (Transaction transaction : transactions) {
            int payerIndex = participants.indexOf(transaction.getPayer());
            int payeeIndex = participants.indexOf(transaction.getPayee());
            graph.addEdge(payerIndex, payeeIndex, transaction.getAmount(), transaction.getCurrency());
        }
        minTransac = graph.minimizeCashFlow(participants);
        
        StyledDocument doc = minimizedTransactionPane.getStyledDocument();
        Style regularStyle = doc.addStyle("regular", null);
        StyleConstants.setFontFamily(regularStyle, "Arial");
        Style boldStyle = doc.addStyle("bold", regularStyle);
        StyleConstants.setBold(boldStyle, true);

        try {
            for(String currency : currenciesEncountered) {
                doc.insertString(doc.getLength(), "  Minimized Cash Flow ( in " + String.format("%s", currency) + " )\n", boldStyle);
                doc.insertString(doc.getLength(), "\n", boldStyle);
                for(Transaction transaction : minTransac) {
                    double amt = transaction.getAmount();
                    double conv_amt = graph.convertAmountFromUSD(amt, currency);
                    doc.insertString(doc.getLength(), transaction.getPayer() + " pays " + String.format("%.2f", conv_amt) + " " + currency + " to " + transaction.getPayee() + "\n", regularStyle);
                }
                doc.insertString(doc.getLength(), "\n", regularStyle);
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private List<String> getParticipants() {
        List<String> participants = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (!participants.contains(transaction.getPayer())) {
                participants.add(transaction.getPayer());
            }
            if (!participants.contains(transaction.getPayee())) {
                participants.add(transaction.getPayee());
            }
        }
        return participants;
    }

    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser("E:/AID Sem II/VS Codes/OOPS/Project");
        fileChooser.setDialogTitle("Import CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToImport = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(fileToImport))) {
                String line;
                boolean isFirstLine = true;
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    if (line.isEmpty()) {
                        break;
                    }
                    String[] values = line.split(",");
                    if (values.length == 4) {
                        String payer = values[0].trim();
                        String payee = values[1].trim();
                        double amount = Double.parseDouble(values[2].trim());
                        String currency = values[3].trim();
                        if (amount <= 0) {
                            JOptionPane.showMessageDialog(this, "Amount in CSV must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        transactions.add(new Transaction(payer, payee, amount, currency));
                        currenciesEncountered.add(currency);
    
                        StyledDocument doc = transactionInputPane.getStyledDocument();
                        Style regularStyle = doc.addStyle("regular", null);
                        StyleConstants.setFontFamily(regularStyle, "Arial");
    
                        try {
                            doc.insertString(doc.getLength(), payer + " pays ", regularStyle);
                            doc.insertString(doc.getLength(), String.format("%.2f", amount), regularStyle);
                            doc.insertString(doc.getLength(), " " + currency + " to " + payee + "\n", regularStyle);
                        } catch (BadLocationException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error reading CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }    

    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser("E:/AID Sem II/VS Codes/OOPS/Project/Minimized");
        fileChooser.setDialogTitle("Export CSV File");
        fileChooser.setSelectedFile(new File("minimized_import_file_name.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToExport = fileChooser.getSelectedFile();

            String filePath = fileToExport.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
                fileToExport = new File(filePath);
            }

            int fileCount = 0;
            while (fileToExport.exists()) {
                fileCount++;
                String newFilePath = filePath.replace(".csv", "(" + fileCount + ").csv");
                fileToExport = new File(newFilePath);
            }

            try (FileWriter writer = new FileWriter(fileToExport)) {
                writer.write("Payer,Payee,Amount,Currency\n");
                for (Transaction transaction : minTransac) {
                    writer.write(String.format("%s,%s,%.2f,%s\n", transaction.getPayer(), transaction.getPayee(), transaction.getAmount(), transaction.getCurrency()));
                }
                JOptionPane.showMessageDialog(this, "Minimized transactions exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error writing to CSV file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearTransactions() {
        currenciesEncountered.clear();
        transactions.clear();
        transactionInputPane.setText("");
        minimizedTransactionPane.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Project_CashFlow_Minimizer();
            }
        });
    }
}

class Edge {
    int from;
    int to;
    double weight;
    String currency;

    public Edge(int from, int to, double weight, String currency) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.currency = currency;
    }
}

class Graph {
    int V;
    List<Edge> edges;
    List<Transaction> minimizedTransactions;
    Map<String, Double> conversionRates;

    public Graph(int V) {
        this.V = V;
        this.edges = new ArrayList<>();
        this.minimizedTransactions = new ArrayList<>();
        this.conversionRates = new HashMap<>();
        initializeConversionRates();
    }

    public void addEdge(int from, int to, double weight, String currency) {
        double convertedWeight = convertAmountToUSD(weight, currency);
        edges.add(new Edge(from, to, convertedWeight, currency));
    }

    public List<Transaction> minimizeCashFlow(List<String> participants) {
        double[] balances = new double[V];
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            balances[edge.from] -= edge.weight;
            balances[edge.to] += edge.weight;
        }
        minimizeCashFlowUtil(balances, participants);
        for (Transaction transaction : minimizedTransactions) {
            String originalCurrency = transaction.getCurrency();
            transaction.setAmount(convertAmountFromUSD(transaction.getAmount(), originalCurrency));
        }
        return minimizedTransactions;
    }

    private void minimizeCashFlowUtil(double[] balances, List<String> participants) {
        int maxCredit = getMaxIndex(balances);
        int maxDebit = getMinIndex(balances);
        
        if (balances[maxCredit] == 0 && balances[maxDebit] == 0) {
            return;
        }
        
        double minAmount = Math.min(-balances[maxDebit], balances[maxCredit]);
        balances[maxCredit] -= minAmount;

        if (balances[maxCredit] < 0) {
        minAmount += balances[maxCredit];
        balances[maxCredit] = 0;
        }

        balances[maxDebit] += minAmount;
        
        String currency = "USD";
        minimizedTransactions.add(new Transaction(participants.get(maxDebit), participants.get(maxCredit), minAmount, currency));
        
        minimizeCashFlowUtil(balances, participants);
    }

    private int getMaxIndex(double[] arr) {
        int maxIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private int getMinIndex(double[] arr) {
        int minIndex = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] < arr[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void initializeConversionRates() {
        conversionRates.put("USD_USD", 1.0); // 1 USD = 1 USD
        conversionRates.put("USD_INR", 83.50); // 1 USD = 73.96 INR
        conversionRates.put("USD_EUR", 0.83); // 1 USD = 0.83 EUR
        conversionRates.put("USD_GBP", 0.79); // 1 USD = 0.79 GBP
        conversionRates.put("USD_CAD", 1.36); // 1 USD = 1.36 CAD
        conversionRates.put("USD_JPY", 156.28); // 1 USD = 156.28 JPY
    }

    public double convertAmountToUSD(double amount, String currency) {
        double conversionRate = conversionRates.getOrDefault("USD_" + currency, 1.0);
        return amount / conversionRate;
    }

    public double convertAmountFromUSD(double amount, String currency) {
        double conversionRate = conversionRates.getOrDefault("USD_" + currency, 1.0);
        return amount * conversionRate;
    }

}

class Transaction {
    private String payer;
    private String payee;
    private double amount;
    private String currency;

    public Transaction(String payer, String payee, double amount, String currency) {
        this.payer = payer;
        this.payee = payee;
        this.amount = amount;
        this.currency = currency;
    }

    public String getPayer() {
        return payer;
    }

    public String getPayee() {
        return payee;
    }

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
