/*
    This file is part of RouteConverter.

    RouteConverter is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    RouteConverter is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RouteConverter; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Copyright (C) 2007 Christian Pesch. All Rights Reserved.
*/

package slash.navigation.converter.gui.dialogs;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import slash.navigation.catalog.domain.RouteCatalog;
import slash.navigation.catalog.domain.exception.DuplicateNameException;
import slash.navigation.converter.gui.ExternalPrograms;
import slash.navigation.converter.gui.RouteConverter;
import slash.navigation.converter.gui.helper.DialogAction;
import slash.navigation.gui.SimpleDialog;
import slash.common.io.Transfer;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Dialog to login a user to the RouteService.
 *
 * @author Christian Pesch
 */

public class LoginDialog extends SimpleDialog {
    private static final Logger log = Logger.getLogger(LoginDialog.class.getName());

    private final RouteCatalog routeCatalog;
    private JPanel contentPane;
    private JTabbedPane tabbedPane;

    private JLabel labelLoginResult;
    private JTextField textFieldLogin;
    private JPasswordField passwordLogin;
    private JButton buttonLogin;
    private JButton buttonCancel1;

    private JLabel labelRegisterResult;
    private JTextField textFieldName;
    private JTextField textFieldFirstName;
    private JTextField textFieldLastName;
    private JTextField textFieldEMail;
    private JPasswordField passwordRegister;
    private JPasswordField passwordRepeat;
    private JButton buttonRegister;
    private JButton buttonCancel2;
    private JCheckBox checkBoxAcceptTerms;
    private JLabel labelAcceptTerms;

    public LoginDialog(RouteCatalog routeCatalog) {
        super(RouteConverter.getInstance().getFrame(), "login");
        this.routeCatalog = routeCatalog;
        setTitle(RouteConverter.getBundle().getString("login-title"));
        setContentPane(contentPane);
        setModal(true);
        setDefaultButton();

        // always have the right default button
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setDefaultButton();
            }
        });

        buttonLogin.addActionListener(new DialogAction(this) {
            public void run() {
                login();
            }
        });

        buttonCancel1.addActionListener(new DialogAction(this) {
            public void run() {
                cancel();
            }
        });

        labelAcceptTerms.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                new ExternalPrograms().startBrowserForTerms(LoginDialog.this);
            }
        });

        buttonRegister.addActionListener(new DialogAction(this) {
            public void run() {
                register();
            }
        });

        buttonCancel2.addActionListener(new DialogAction(this) {
            public void run() {
                cancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                cancel();
            }
        });

        textFieldLogin.setText(RouteConverter.getInstance().getUserNamePreference());

        contentPane.registerKeyboardAction(new DialogAction(this) {
            public void run() {
                cancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void setDefaultButton() {
        if (tabbedPane.getSelectedIndex() == 0)
            getRootPane().setDefaultButton(buttonLogin);
        else
            getRootPane().setDefaultButton(buttonRegister);
    }

    private boolean successful = false;

    public boolean isSuccessful() {
        return successful;
    }

    private void login(String userName, String password) {
        routeCatalog.setAuthentication(userName, password);
        RouteConverter.getInstance().setUserNamePreference(userName, password);
    }

    private void register(String userName, String password, String firstName, String lastName, String email) throws IOException {
        routeCatalog.addUser(userName, password, firstName, lastName, email);
    }

    private void login() {
        String name = textFieldLogin.getText();
        if (Transfer.trim(name) == null) {
            labelLoginResult.setText("Error: No name given!"); // TODO make nicer
            pack();
            return;
        }
        String password = new String(passwordLogin.getPassword());
        if (Transfer.trim(password) == null) {
            labelLoginResult.setText("Error: No password given!"); // TODO make nicer
            pack();
            return;
        }

        login(name, password);
        successful = true;
        dispose();
    }

    private void register() {
        String userName = textFieldName.getText();
        if (Transfer.trim(userName) == null) {
            labelRegisterResult.setText("Error: No user name given!"); // TODO make nicer
            pack();
            return;
        }
        if (userName.length() < 4) {
            labelRegisterResult.setText("Error: User name too short; at least 4 characters required!"); // TODO make nicer
            pack();
            return;
        }

        String email = textFieldEMail.getText();
        if (Transfer.trim(email) == null) {
            labelRegisterResult.setText("Error: No email given!"); // TODO make nicer
            pack();
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            labelRegisterResult.setText("Error: No valid email given; at least @ and . required!"); // TODO make nicer
            pack();
            return;
        }

        String password = new String(passwordRegister.getPassword());
        if (Transfer.trim(password) == null) {
            labelRegisterResult.setText("Error: No password given!"); // TODO make nicer
            pack();
            return;
        }
        if (password.length() < 4) {
            labelRegisterResult.setText("Error: Password too short; at least 4 characters required!"); // TODO make nicer
            pack();
            return;
        }
        String repeat = new String(passwordRepeat.getPassword());
        if (!password.equals(repeat)) {
            labelRegisterResult.setText("Error: Passwords do not match!"); // TODO make nicer
            pack();
            return;
        }

        if (!checkBoxAcceptTerms.isSelected()) {
            labelRegisterResult.setText("Error: Terms not accepted!"); // TODO make nicer
            pack();
            return;
        }

        try {
            register(userName, password, textFieldFirstName.getText(), textFieldLastName.getText(), email);
            labelRegisterResult.setText("Successfully registred user!"); // TODO make nicer
            pack();
            login(userName, password);
            successful = true;
            dispose();
        } catch (DuplicateNameException e) {
            labelRegisterResult.setText("Error: User name already exists!"); // TODO make nicer
            pack();
        } catch (Throwable t) {
            log.severe("Could not register: " + t.getMessage());
            labelRegisterResult.setText("<html>Could not register:<p>" +
                    t.getMessage() + "<p>" +
                    "Please write an error report to <a href=\"mailto:support@routeconverter.com\">support@routeconverter.com</a>."); // TODO make nicer
            pack();
        }
    }

    private void cancel() {
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        tabbedPane = new JTabbedPane();
        contentPane.add(tabbedPane, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(200, 200), null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(6, 2, new Insets(10, 10, 10, 10), -1, -1));
        tabbedPane.addTab(ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("login"), panel1);
        final JLabel label1 = new JLabel();
        this.$$$loadLabelText$$$(label1, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("username-colon"));
        panel1.add(label1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldLogin = new JTextField();
        textFieldLogin.setText("");
        panel1.add(textFieldLogin, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordLogin = new JPasswordField();
        panel1.add(passwordLogin, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        this.$$$loadLabelText$$$(label2, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("password-colon"));
        panel1.add(label2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonLogin = new JButton();
        this.$$$loadButtonText$$$(buttonLogin, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("login"));
        panel2.add(buttonLogin, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonCancel1 = new JButton();
        this.$$$loadButtonText$$$(buttonCancel1, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("cancel"));
        panel2.add(buttonCancel1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        this.$$$loadLabelText$$$(label3, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("please-login"));
        panel1.add(label3, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelLoginResult = new JLabel();
        labelLoginResult.setText("");
        panel1.add(labelLoginResult, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(10, 2, new Insets(10, 10, 20, 10), -1, -1));
        tabbedPane.addTab(ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("register"), panel4);
        final JLabel label4 = new JLabel();
        this.$$$loadLabelText$$$(label4, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("username-colon"));
        panel4.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        this.$$$loadLabelText$$$(label5, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("password-colon"));
        panel4.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Repeat password");
        panel4.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldName = new JTextField();
        panel4.add(textFieldName, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("EMail");
        panel4.add(label7, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldEMail = new JTextField();
        panel4.add(textFieldEMail, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordRegister = new JPasswordField();
        panel4.add(passwordRegister, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordRepeat = new JPasswordField();
        panel4.add(passwordRepeat, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonRegister = new JButton();
        this.$$$loadButtonText$$$(buttonRegister, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("register"));
        panel5.add(buttonRegister, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel5.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        buttonCancel2 = new JButton();
        this.$$$loadButtonText$$$(buttonCancel2, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("cancel"));
        panel5.add(buttonCancel2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label8 = new JLabel();
        this.$$$loadLabelText$$$(label8, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("please-register"));
        panel4.add(label8, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        labelRegisterResult = new JLabel();
        labelRegisterResult.setText("");
        panel4.add(labelRegisterResult, new GridConstraints(8, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label9 = new JLabel();
        label9.setText("First name");
        panel4.add(label9, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Last name");
        panel4.add(label10, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textFieldFirstName = new JTextField();
        panel4.add(textFieldFirstName, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        textFieldLastName = new JTextField();
        panel4.add(textFieldLastName, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        labelAcceptTerms = new JLabel();
        this.$$$loadLabelText$$$(labelAcceptTerms, ResourceBundle.getBundle("slash/navigation/converter/gui/RouteConverter").getString("register-accept-terms"));
        panel4.add(labelAcceptTerms, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkBoxAcceptTerms = new JCheckBox();
        checkBoxAcceptTerms.setText("");
        panel4.add(checkBoxAcceptTerms, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadLabelText$$$(JLabel component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setDisplayedMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    private void $$$loadButtonText$$$(AbstractButton component, String text) {
        StringBuffer result = new StringBuffer();
        boolean haveMnemonic = false;
        char mnemonic = '\0';
        int mnemonicIndex = -1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '&') {
                i++;
                if (i == text.length()) break;
                if (!haveMnemonic && text.charAt(i) != '&') {
                    haveMnemonic = true;
                    mnemonic = text.charAt(i);
                    mnemonicIndex = result.length();
                }
            }
            result.append(text.charAt(i));
        }
        component.setText(result.toString());
        if (haveMnemonic) {
            component.setMnemonic(mnemonic);
            component.setDisplayedMnemonicIndex(mnemonicIndex);
        }
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
