
import Database.EventDatabase;
import System.*;
import System.Event;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
// This GUI would display the events available on our System for the User to select and add events to their cart
public class EventPage {
    // Declare variables and objects
    private String path= "EventDatabase.txt";
    public JPanel rootPanel;
    private JPanel Table;
    private JPanel Title;
    private JPanel Input;
    private JButton btnAdd;
    private JTable showTable;
    private JTextField txtshow;
    private JTable cartTable;
    private JButton btnPay;
    private JTextArea txtTotal;
    private JButton btnViewEvent;

    private Object[][] data;
    public ArrayList<Event> events=new ArrayList<>();
    public boolean flag= false;
    private double total;
    private Customer c1;
    public EventDatabase eventDatabase;


    public EventPage(Customer c1){
        // Initialize variables and objects
        eventDatabase= new EventDatabase(); // create event database
        this.c1 = c1; // create customer that was passed in
        total=0;
        showTable.setDefaultEditor(Object.class, null);// Disables editing of table cells

       createTable();// Creates and populates the table of available events
        updateTotal();// Updates the total cost displayed

        // Listener for when a row is clicked in the available events table
        showTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                flag= true;// Set flag to indicate an event has been selected

                // Get the selected row's data and display the event name in the text field
                int index = showTable.getSelectedRow();
                TableModel model = showTable.getModel();
                String value1 =model.getValueAt(index,0).toString().trim();
                String value2 =model.getValueAt(index,1).toString().trim();
                String value3 =(model.getValueAt(index,2).toString()).trim();
                String value4 =model.getValueAt(index,3).toString().trim();
                String value5 =model.getValueAt(index,4).toString().trim();
                txtshow.setText(value1);



            }
        });
        // Listener for when the "Add to Cart" button is clicked
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                // Get the selected row's data and add it to the cart table
                int index = showTable.getSelectedRow();
                Object[] row = new Object[5];
                DefaultTableModel model = (DefaultTableModel) showTable.getModel();
                DefaultTableModel model2 = (DefaultTableModel) cartTable.getModel();
                    row[0]=model.getValueAt(index,0);
                    row[1]=model.getValueAt(index,1);
                    row[2]=model.getValueAt(index,2);
                    row[3]=model.getValueAt(index,3);
                    row[4]=model.getValueAt(index,4);
                    int ticket = Integer.parseInt(row[3].toString().trim());
                    if (ticket > 0) { // checks available tickets
                        model2.addRow(row);

                    }else{
                        txtshow.setText("Event's ticket is currently unavailable");
                    }
                updateTotal();




            }
        });
        // Add a mouse listener to the cartTable for handling clicks on the table
        cartTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = cartTable.getSelectedRow();
                if (selectedRow != -1) {
                    DefaultTableModel model = (DefaultTableModel) cartTable.getModel();
//                    sum=sum-Double.parseDouble(model.getValueAt(selectedRow,4).toString().trim());
                    model.removeRow(selectedRow);
                    updateTotal();

                }

            }
        });
        // Add an action listener to the btnPay button for initiating payment
        btnPay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add the selected events to the cart
                for (int i = 0; i < cartTable.getRowCount(); i++) {
                    String name = cartTable.getValueAt(i, 0).toString();
                    String location = cartTable.getValueAt(i, 1).toString();
                    String date = cartTable.getValueAt(i, 2).toString();
                    int tickets = Integer.parseInt(cartTable.getValueAt(i, 3).toString().trim());
                    double price = Double.parseDouble(cartTable.getValueAt(i, 4).toString().trim());
                    events.add(new Event(name,location,date,tickets,price));
                }
                // Set the cart items in the Cart object
                Cart cart = new Cart();
                cart.setCartItems(events);
                c1.setCart(cart);
                // Open the payment window
                JFrame startFrame1 = (JFrame) SwingUtilities.getWindowAncestor(btnPay); // got from ChatGPT
                //startFrame1.setVisible(false);
                JFrame payment = new JFrame("Payment");
                payment.setContentPane(new Payment(c1, total).paymentPanel);
                payment.setTitle("Payment");
                payment.setSize(1000,800);
                payment.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                payment.setLocationRelativeTo(null);
                payment.setVisible(true);


            }

        });
        // Add an action listener to the btnViewEvent button for displaying event details
        btnViewEvent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (flag) {
                    // Get the selected row from the showTable
                    int index = showTable.getSelectedRow();
                    TableModel model = showTable.getModel();
                    // Get the values of the selected row
                    String value1 = model.getValueAt(index, 0).toString().trim();
                    String value2 = model.getValueAt(index, 1).toString().trim();
                    String value3 = model.getValueAt(index, 2).toString().trim();
                    String value4 = model.getValueAt(index, 3).toString().trim();
                    String value5 = model.getValueAt(index, 4).toString().trim();

                    // Display the event details in a new window
                    JFrame startFrame = new JFrame("Home");
                    startFrame.setContentPane(new EventInfo(value1, value2, value3, value4, value5).panel1);
                    startFrame.setPreferredSize(new Dimension(800, 700));
                    startFrame.pack();
                    startFrame.setVisible(true);
                }else{
                    // Display an error message if no event is selected
                    txtshow.setText("Please Select an Event");
                }

            }
        });
    }
    // Update the total cost of the cart
    private void updateTotal() { // updates total cost
        txtTotal.setOpaque(false);
        double subtotal = 0;
        for (int i = 0; i < cartTable.getRowCount(); i++) {
            double price = (double) cartTable.getValueAt(i, 4);
            subtotal += price;
        }
        double tax = subtotal * 0.13;
        total = subtotal + tax;
        String totalStr = String.format("Subtotal: %.2f%nTax: %.2f%nTotal: %.2f", subtotal, tax, total);
        txtTotal.setText(totalStr);
    }


    private void createTable() { // creates GUI table for events
        ArrayList<Event> events = getData();
        data = new Object[events.size()][5];
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            data[i] = new Object[]{event.getName(), event.getLocation(), event.getDate(), event.getNumberofTickets(), event.getPrice()};
        }

        showTable.setModel(new DefaultTableModel(
                data,
                new String[]{"Event Name", "Location", "Date",  "Tickets", "Price"}
        ));
        cartTable.setModel(new DefaultTableModel(
                null,
                new String[]{"Event Name", "Location", "Date",  "Tickets", "Price"}
        ));
        TableColumnModel columns=showTable.getColumnModel();
        columns.getColumn( 0).setMinWidth (100);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment (JLabel.CENTER);
        columns.getColumn( 1).setMinWidth (100);
        columns.getColumn( 2).setMinWidth (100);
        columns.getColumn (3).setCellRenderer(centerRenderer);



    }
    public ArrayList<Event> getData() { // gets Data from database
        ArrayList<Event> events = new ArrayList<Event>();
        File file = new File(path);
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(",");
                String name = parts[0];
                String location = parts[1];
                String date = parts[2];
                String numberOfTicketsStr = parts[3].trim();
                int numberOfTickets = Integer.parseInt(numberOfTicketsStr);
                String priceStr = parts[4].trim();
                double price = Double.parseDouble(priceStr);
                Event event = new Event(name, location, date, numberOfTickets, price);
                events.add(event);
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file: " + e.getMessage());
        }
        return events;
    }





}
