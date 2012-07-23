/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cakephp.netbeans.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.cakephp.netbeans.CakePhpFrameworkProvider;
import org.cakephp.netbeans.options.CakePhpPlugin;
import org.cakephp.netbeans.ui.actions.ClearCacheAction;
import org.cakephp.netbeans.util.CakeZip;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.actions.BaseAction;
import org.openide.*;
import org.openide.WizardDescriptor.Panel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

// An example action demonstrating how the wizard could be called from within
// your code. You can move the code below wherever you need, or register an action:
@ActionID(category = "PHP", id = "org.cakephp.netbeans.ui.wizards.InstallPluginsWizardAction")
@ActionRegistration(displayName = "Install Plugins")
// @ActionReference(path="Menu/Tools", position=...)
public final class InstallPluginsWizardAction extends BaseAction implements ActionListener {
    private static final String LF = "\n";

    private static final long serialVersionUID = 4405963698419409213L;
    private static InstallPluginsWizardAction INSTANCE = new InstallPluginsWizardAction();

    private InstallPluginsWizardAction() {
    }

    public static InstallPluginsWizardAction getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getFullName() {
        return NbBundle.getMessage(ClearCacheAction.class, "LBL_CakePhpAction", getPureName());
    }

    @Override
    protected String getPureName() {
        return NbBundle.getMessage(InstallPluginsWizardAction.class, "LBL_InstallPlugin");
    }

    @Override
    protected void actionPerformed(PhpModule pm) {
        List<WizardDescriptor.Panel<WizardDescriptor>> panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        panels.add(new InstallPluginsWizardPanel());
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            // Default step name to component name of panel.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, true);
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, true);
            }
        }
        WizardDescriptor wiz = new WizardDescriptor(new WizardDescriptor.ArrayIterator<WizardDescriptor>(panels));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wiz.setTitleFormat(new MessageFormat("{0}"));
        wiz.setTitle("CakePHP Install Plugins");
        if (DialogDisplayer.getDefault().notify(wiz) == WizardDescriptor.FINISH_OPTION) {
            Panel<WizardDescriptor> panel = (InstallPluginsWizardPanel) panels.get(0);
            InstallPluginsVisualPanel component = (InstallPluginsVisualPanel) panel.getComponent();
            String installPath = component.getInstallPathTextField();
            final FileObject fo = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(installPath);
            NotifyDescriptor descriptor = null;
            if (fo == null) {
                descriptor = new NotifyDescriptor.Message(installPath + " dosen't exist.", NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(descriptor);
                return;
            }
            final List<CakePhpPlugin> plugins = component.getCakePhpPluginList();
            // error list
            final StringBuilder errors = new StringBuilder();

            // create modeless dialog
            final InstallStatusDisplayPanel installStatusDisplayPanel = new InstallStatusDisplayPanel();
            DialogDescriptor installStatusDescriptor = new DialogDescriptor(installStatusDisplayPanel, "Install status");
            final Dialog installStatusDialog = DialogDisplayer.getDefault().createDialog(installStatusDescriptor);
            installStatusDialog.setModal(false);
            installStatusDialog.setVisible(true);
            
            new Thread(new Runnable(){

                @Override
                public void run() {
                    // unzip
                    for (CakePhpPlugin plugin : plugins) {
                        if (plugin.isInstall()) {
                            String pluginName = plugin.getName();
                            // set status
                            installStatusDisplayPanel.getDisplayStatusTextArea().append(pluginName + " : ");
                            CakeZip zip = new CakeZip(pluginName);
                            try {
                                zip.unzip(plugin.getUrl(), fo);
                                installStatusDisplayPanel.getDisplayStatusTextArea().append("Done" + LF);
                            } catch (IOException ex) {
                                errors.append(pluginName).append(LF);
                                installStatusDisplayPanel.getDisplayStatusTextArea().append("Error" + LF);
                            }
                        }
                    }
                    String errorMessage = errors.toString();
                    NotifyDescriptor d = null;
                    if (!errorMessage.isEmpty()) { //display error dialog
                        d = new NotifyDescriptor.Message("Please confirm the URL.\n" + errorMessage, NotifyDescriptor.ERROR_MESSAGE);
                    } else { // display complete dialog
                        d = new NotifyDescriptor.Message("Install Complete!", NotifyDescriptor.INFORMATION_MESSAGE);
                    }
                    DialogDisplayer.getDefault().notifyLater(d);

                    SwingUtilities.invokeLater(new Runnable(){

                        @Override
                        public void run() {
                            installStatusDialog.setVisible(false);
                            installStatusDialog.dispose();
                        }
                        
                    });
                }
                
            }).start();
        }
    }
}