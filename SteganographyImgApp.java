import java.awt.image.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.*;

    public class SteganographyImgApp extends JFrame implements ActionListener {
    JButton open = new JButton("Open Image"), embed = new JButton("Embed Message"), decode = new JButton("Extract Message");
    JTextArea message = new JTextArea(10, 3);
    BufferedImage sourceImage = null;
    BufferedImage embeddedImage = null;

    private JPanel imagePanel = new JPanel();
    private String embedPassword; // Store password for embedding
    private String decodeMessageFromImage(BufferedImage img, String password) {
        int messageLength = extractInteger(img, 0, 0);
        byte[] result = new byte[messageLength];

        for (int i = 0; i < messageLength; i++)
            result[i] = extractByte(img, i * 8 + 32, 0);

        return new String(result);
    }

    public SteganographyImgApp() {
        super("Steganography Image Application");
        assembleInterface();
        this.setSize(1000, 700);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setVisible(true);
    }

    private void assembleInterface() {
        JPanel p = new JPanel(new FlowLayout());
        p.add(open);
        p.add(embed);
        p.add(decode);
        this.getContentPane().add(p, BorderLayout.SOUTH);
        open.addActionListener(this);
        embed.addActionListener(this);
        decode.addActionListener(this);
        open.setMnemonic('O');
        embed.setMnemonic('E');
        decode.setMnemonic('D');

        p = new JPanel(new GridLayout(1, 1));
        p.add(new JScrollPane(message));
        message.setFont(new Font("Arial", Font.BOLD, 20));
        p.setBorder(BorderFactory.createTitledBorder("Message / Decoded Message"));
        this.getContentPane().add(p, BorderLayout.NORTH);
        this.getContentPane().add(imagePanel, BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent ae) {
        Object o = ae.getSource();
        if (o == open) {
            openImage();
        } else if (o == embed) {
            embedMessage();
        } else if (o == decode) {
            decodeMessage();
        }
    }

    private void clearImagePanel() {
    imagePanel.removeAll();
    imagePanel.setPreferredSize(new Dimension(0, 0)); // Reset panel size
    this.revalidate();
    this.repaint();
}

    private void openImage() {
    java.io.File f = showFileDialog(true);
    try {
        sourceImage = ImageIO.read(f);

        // Set a maximum size for the imagePanel
        int maxImagePanelWidth = 600; // Set your desired maximum width
        int maxImagePanelHeight = 400; // Set your desired maximum height

        // Calculate the scale factor for resizing the image
        double widthScale = (double) maxImagePanelWidth / sourceImage.getWidth();
        double heightScale = (double) maxImagePanelHeight / sourceImage.getHeight();
        double scale = Math.min(widthScale, heightScale);

        // Create a scaled image
        int scaledWidth = (int) (sourceImage.getWidth() * scale);
        int scaledHeight = (int) (sourceImage.getHeight() * scale);
        Image scaledImage = sourceImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

        // Create a JLabel to display the scaled image
        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));

        // Set the layout of the imagePanel to null
        imagePanel.setLayout(null);

        // Set the bounds of the imageLabel within the limited size imagePanel
        int x = (maxImagePanelWidth - scaledWidth) / 2;
        int y = (maxImagePanelHeight - scaledHeight) / 2;
        imageLabel.setBounds(x, y, scaledWidth, scaledHeight);

        // Set the preferred size of the imagePanel
        imagePanel.setPreferredSize(new Dimension(maxImagePanelWidth, maxImagePanelHeight));

        // Add the image label to the image panel
        imagePanel.removeAll(); // Remove any existing components
        imagePanel.add(imageLabel);

        // Refresh the UI to display the image
        this.revalidate();
        this.repaint();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

    private void embedMessage() {
        String enteredPassword;
        boolean validPassword = false;

         // Keep prompting for a valid password
         do {
            enteredPassword = JOptionPane.showInputDialog(this, "Create a password for embedding:");
    
            if (enteredPassword == null) {
                // Cancelled password creation
                return; // Exit the method
            }
    
            if (isValidPassword(enteredPassword)) {
                validPassword = true;
            } else {
                JOptionPane.showMessageDialog(this, "Password should contain an uppercase,lowercase,number,specialcharacter. ", "Password Error", JOptionPane.ERROR_MESSAGE);
            }
        } while (!validPassword);
  
        embedPassword = enteredPassword; // Set the valid password

  
         String mess = message.getText();
    embeddedImage = sourceImage.getSubimage(0, 0, sourceImage.getWidth(), sourceImage.getHeight());
    embedMessage(embeddedImage, mess);

    boolean fileExists = true;
        java.io.File selectedFile = null;

        while (fileExists) {
    // Show a save dialog to let the user specify the filename
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Embedded Image");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    int returnValue = fileChooser.showSaveDialog(this);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
        selectedFile = fileChooser.getSelectedFile();         
        // Ensure the selected file has the ".png" extension
        String filename = selectedFile.toString();
        if (!filename.toLowerCase().endsWith(".png")) {
            selectedFile = new java.io.File(filename + ".png");
        }
         // Check if the file with the same name already exists
         if (selectedFile.exists()) {
            JOptionPane.showMessageDialog(this, "An image with the same name already exists!", "File Exists", JOptionPane.WARNING_MESSAGE);
        } else {
            fileExists = false;
        }
    } else {
        // If the user cancels the dialog, exit the loop
        fileExists = false;
    }
}

if (selectedFile != null) {

        // Save the embedded image to the selected file with the ".png" extension
        saveImage(embeddedImage, selectedFile.toString());

         // Get the image file name without extension
         String imageFileName = selectedFile.getName().replaceFirst("[.][^.]+$", "");

         // Save the password with the image name
         savePasswordWithImageName(embedPassword, imageFileName);
    }
    } 

    private boolean isValidPassword(String password) {
    // Check for minimum length
    if (password.length() < 6 || password.length() > 15) {
        return false;
    }

    // Check for at least one uppercase letter
    if (!password.matches(".*[A-Z].*")) {
        return false;
    }

    // Check for at least one lowercase letter
    if (!password.matches(".*[a-z].*")) {
        return false;
    }

    // Check for at least one special character
    if (!password.matches(".*[!@#$%^&*()_].*")) {
        return false;
    }

    // Check for at least one number
    if (!password.matches(".*\\d.*")) {
        return false;
    }

    return true;
}

    private void savePasswordWithImageName(String password, String imageName) {
	   Map<String, String> passwordMap = new LinkedHashMap<>();// Read existing passwords if any

       passwordMap.put(imageName, password);

    savePasswordsToFile(passwordMap);
}

    private void savePasswordsToFile(Map<String, String> passwordMap) {
    try (FileWriter writer = new FileWriter("passwords.txt", true)) {
    	String lastValue = null;
    	String lastKey = null;
    	
    	for (Entry<String, String> entry : passwordMap.entrySet()) {
             lastValue = entry.getValue();
             lastKey = entry.getKey();
            
        }
    	    
            writer.write("Image is: " + lastKey + ".png password: " + lastValue + "\n");
             writer.close();
       
    } catch (IOException ex) {
        ex.printStackTrace();
    }
}

    private Map<String, String> readPasswordsFromFile() {
    Map<String, String> passwordMap = new LinkedHashMap<>();

    try {
        File passwordFile = new File("passwords.txt");
        if (!passwordFile.exists()) {
            passwordFile.createNewFile(); // Create the file if it doesn't exist
        } else {
            Scanner scanner = new Scanner(passwordFile);
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(":");
                
                String s = parts[1];
                int index = s.indexOf(".png");
                String key = s.substring(1,index);
                String value = parts[2].substring(1);
                if (parts.length == 3) {
                    passwordMap.put(key, value);
                }
            }
            scanner.close();
        }
    } catch (IOException ex) {
        ex.printStackTrace();
    }

    return passwordMap;
}

    private void decodeMessage() {
    clearDecoding(); // Clear both image and message

    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Select an Image for Message Decoding");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    int returnValue = fileChooser.showOpenDialog(this);

    if (returnValue == JFileChooser.APPROVE_OPTION) {
        java.io.File selectedFile = fileChooser.getSelectedFile();

        String enteredPassword = JOptionPane.showInputDialog(this, "Enter password for decoding:");

        // Retrieve all stored passwords
        Map<String, String> passwordMap = readPasswordsFromFile();

        // Extract the image file name without extension
        String imageFileName = selectedFile.getName().replaceFirst("[.][^.]+$", "");

        // Check if the entered password matches the stored password for the selected image
        if (passwordMap.containsKey(imageFileName)) {
            String storedPassword = passwordMap.get(imageFileName);
            if (enteredPassword != null && enteredPassword.equals(storedPassword)) {
                try {
                    BufferedImage selectedImage = ImageIO.read(selectedFile);

                    // Decode the message using the entered password
                    String decodedMessage = decodeMessageFromImage(selectedImage, enteredPassword);
                    if (decodedMessage != null && !decodedMessage.isEmpty()) {
                        message.setText(decodedMessage); // Set the decoded message
                        displayImage(selectedImage);  // Display the image
                    } else {
                        JOptionPane.showMessageDialog(this, "Incorrect password or no message found!", "Decoding Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Wrong password!", "Password Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Password not found for the selected image!", "Password Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    // clear both the message text area and the displayed image
    private void clearDecoding() {
    message.setText(""); // Clear the message text area
    clearImagePanel(); // Clear the displayed image
}

    private void displayImage(BufferedImage img) {
    int maxImagePanelWidth = 600;
    int maxImagePanelHeight = 400;

    double widthScale = (double) maxImagePanelWidth / img.getWidth();
    double heightScale = (double) maxImagePanelHeight / img.getHeight();
    double scale = Math.min(widthScale, heightScale);

    int scaledWidth = (int) (img.getWidth() * scale);
    int scaledHeight = (int) (img.getHeight() * scale);
    Image scaledImage = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);

    JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
    imagePanel.setLayout(null);
    int x = (maxImagePanelWidth - scaledWidth) / 2;
    int y = (maxImagePanelHeight - scaledHeight) / 2;
    imageLabel.setBounds(x, y, scaledWidth, scaledHeight);

    imagePanel.removeAll();
    imagePanel.add(imageLabel);
    imagePanel.setPreferredSize(new Dimension(maxImagePanelWidth, maxImagePanelHeight));
    this.revalidate();
    this.repaint();
}

    private java.io.File showFileDialog(boolean open) {
        JFileChooser fc = new JFileChooser("Open an image");
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(java.io.File f) {
                String name = f.getName().toLowerCase();
                if (open)
                return f.isDirectory() || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".bmp");
                return open;
            }

            public String getDescription() {
               if (open)
                return "Image (*.png, *.jpg, *.jpeg,  *.bmp)";
               return "Image (*.png)";
            }
        });

        java.io.File f = null;
        if (open && fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            f = fc.getSelectedFile();
        return f != null ? f : null;
    }

    private void embedMessage(BufferedImage img, String mess) {
        int messageLength = mess.length();
        int imageWidth = img.getWidth(), imageHeight = img.getHeight();
        int imageSize = imageWidth * imageHeight;

        if (messageLength * 8 + 32 > imageSize) {
            JOptionPane.showMessageDialog(this, "Message is too long for the chosen image", "Message too long!",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        embedInteger(img, messageLength, 0, 0);

        byte[] b = mess.getBytes();
        for (int i = 0; i < b.length; i++)
            embedByte(img, b[i], i * 8 + 32, 0);
    }

    private void embedInteger(BufferedImage img, int n, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight();
        int startX = start / maxY, startY = start - startX * maxY, count = 0;
        for (int i = startX; i < maxX && count < 32; i++) {
            for (int j = startY; j < maxY && count < 32; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(n, count);
                rgb = setBitValue(rgb, storageBit, bit);
                img.setRGB(i, j, rgb);
                count++;
            }
        }
    }

    private void embedByte(BufferedImage img, byte b, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight();
        int startX = start / maxY, startY = start - startX * maxY, count = 0;
        for (int i = startX; i < maxX && count < 8; i++) {
            for (int j = startY; j < maxY && count < 8; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(b, count);
                rgb = setBitValue(rgb, storageBit, bit);
                img.setRGB(i, j, rgb);
                count++;
            }
        }
    }

    private void saveImage(BufferedImage img, String filename) {
        try {
            ImageIO.write(img, "PNG", new java.io.File(filename));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int extractInteger(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight();
        int startX = start / maxY, startY = start - startX * maxY, count = 0;
        int length = 0;
        for (int i = startX; i < maxX && count < 32; i++) {
            for (int j = startY; j < maxY && count < 32; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(rgb, storageBit);
                length = setBitValue(length, count, bit);
                count++;
            }
        }
        return length;
    }

    private byte extractByte(BufferedImage img, int start, int storageBit) {
        int maxX = img.getWidth(), maxY = img.getHeight();
        int startX = start / maxY, startY = start - startX * maxY, count = 0;
        byte b = 0;
        for (int i = startX; i < maxX && count < 8; i++) {
            for (int j = startY; j < maxY && count < 8; j++) {
                int rgb = img.getRGB(i, j);
                int bit = getBitValue(rgb, storageBit);
                b = (byte) setBitValue(b, count, bit);
                count++;
            }
        }
        return b;
    }

    private int getBitValue(int n, int location) {
        int v = n & (int) Math.round(Math.pow(2, location));
        return v == 0 ? 0 : 1;
    }

        private int setBitValue(int n, int location, int bit) {
        int toggle = (int) Math.pow(2, location);
        int bv;
        bv = getBitValue(n, location);
        if (bv == bit)
            return n;
        if (bv == 0 && bit == 1)
            n |= toggle;
        else if (bv == 1 && bit == 0)
            n ^= toggle;
        return n;
    }
    public static void main(String[] args) {
        new SteganographyImgApp();
    }
}