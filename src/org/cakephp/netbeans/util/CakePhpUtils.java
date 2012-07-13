/*
 * TODO: add license
 */

package org.cakephp.netbeans.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import org.cakephp.netbeans.CakePhpFrameworkProvider;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

public final class CakePhpUtils {
    public static final int CAKE_VERSION_MAJOR = 0;
    public static final int CAKE_VERSION_MINOR = 1;
    public static final int CAKE_VERSION_REVISION = 2;
    public static final int CAKE_VERSION_NOT_STABLE = 3;
    private static final String APP = "App"; // NOI18N
    private static final String DIR_APP_MODEL = "app/Model"; // NOI18N
    private static final String BEHAVIOR = "Behavior"; // NOI18N
    private static final String CAKE_AUTO_COMPLETION_PHP = "_cake_auto_completion_.php"; // NOI18N
    private static final String COMPONENT = "Component"; // NOI18N
    private static final String CONTROLLER_CLASS_SUFFIX = "Controller"; // NOI18N
    private static final String CONTROLLER_FILE_SUFIX = "_controller"; // NOI18N
    private static final String CONTROLLER_FILE_SUFIX_2 = "Controller"; // NOI18N cake2.x.x
    private static final String DIR_APP_CONTROLLER_COMPONENT = "app/Controller/Component"; // NOI18N
    private static final String DIR_APP_MODEL_BEHAVIOR = "app/Model/Behavior"; // NOI18N
    private static final String DIR_APP_PLUGIN = "app/Plugin"; // NOI18N
    private static final String DIR_APP_VIEW_HELPER = "app/View/Helper"; // NOI18N
    private static final String DIR_CONTROLLERS = "controllers"; // NOI18N
    private static final String DIR_CONTROLLER_2 = "Controller"; // NOI18N cake2.x.x
    private static final String DIR_LIB_CAKE_MODEL_BEHAVIOR = "lib/Cake/Model/Behavior"; // NOI18N
    private static final String DIR_NBPROJECT = "nbproject"; // NOI18N
    private static final String DIR_PLUGINS = "plugins"; // NOI18N
    private static final String DIR_VIEWS = "views"; // NOI18N
    private static final String DIR_VIEW_2 = "View"; // NOI18N cake2.x.x
    private static final String EMPTY_FILE = "empty"; // NOI18N
    private static final String FILE_VIEW_EXT = "ctp"; // NOI18N
    private static final String HELPER = "Helper"; // NOI18N
    private static final String PROPERTY_ANNOTATION_PATTERN = " * @property %s $%s"; // NOI18N
    private static final String UNDERSCORE = "_"; // NOI18N
    private static final String DIR_THEMED = "themed"; // NOI18N 
    private static final String DIR_THEMED_2 = "Themed"; // NOI18N 

    private static final String FILE_CONTROLLER_RELATIVE = "../../" + DIR_CONTROLLERS + "/%s.php"; // NOI18N
    private static final String FILE_CONTROLLER_RELATIVE_2 = "../../" + DIR_CONTROLLER_2 + "/%s.php"; // NOI18N cake2.0
    private static final String FILE_THEME_CONTROLLER_RELATIVE = "../../../../" + DIR_CONTROLLERS + "/%s.php"; // NOI18N
    private static final String FILE_THEME_CONTROLLER_RELATIVE_2 = "../../../../" + DIR_CONTROLLER_2 + "/%s.php"; // NOI18N cake2.0
    private static final String FILE_VIEW_RELATIVE = "../" + DIR_VIEWS + "/%s/%s." + FILE_VIEW_EXT; // NOI18N
    private static final String FILE_VIEW_RELATIVE_2 = "../" + DIR_VIEW_2 + "/%s/%s." + FILE_VIEW_EXT; // NOI18N cake2.0
    private static final String FILE_THEME_VIEW_RELATIVE = "../" + DIR_VIEWS + "/" + DIR_THEMED + "/%s/%s/%s." + FILE_VIEW_EXT; // NOI18N
    private static final String FILE_THEME_VIEW_RELATIVE_2 = "../" + DIR_VIEW_2 + "/" + DIR_THEMED_2 + "/%s/%s/%s." + FILE_VIEW_EXT; // NOI18N cake2.0

    private CakePhpUtils() {
    }

    public static boolean isView(FileObject fo) {
        if (!fo.isData() || !fo.getExt().equals(FILE_VIEW_EXT)) {
            return false;
        }
        // Theme view file  View/Themed/ThemeName/Controller/View.ctp
        if (DIR_VIEW_2.equals(fo.getFileObject("../../../../").getName())
            || DIR_VIEWS.equals(fo.getFileObject("../../../../").getName())) { // NOI18N
            return true;
        }
        File file = FileUtil.toFile(fo);
        File parent = file.getParentFile(); // controller
        if (parent == null) {
            return false;
        }
        parent = parent.getParentFile(); // scripts
        if (parent == null) {
            return false;
        }
        // cake 2.x.x
        if (DIR_VIEW_2.equals(parent.getName())) {
            return true;
        }
        return DIR_VIEWS.equals(parent.getName());
    }

    public static FileObject getView(FileObject controller, PhpBaseElement phpElement) {
        FileObject view = null;
        if (phpElement instanceof PhpClass.Method) {
            view = getView(controller, getViewFileName(phpElement.getName()));
        }
        return view;
    }

    private static FileObject getView(FileObject controller, String viewName) {
        File parent = FileUtil.toFile(controller).getParentFile();
        File view = PropertyUtils.resolveFile(parent, String.format(FILE_VIEW_RELATIVE, getViewFolderName(controller.getName()), viewName));
        if (!view.isFile()) {
            view = PropertyUtils.resolveFile(parent, String.format(FILE_VIEW_RELATIVE_2, getViewFolderName(controller.getName()), viewName));
        }
        if (view.isFile()) {
            return FileUtil.toFileObject(view);
        }
        return null;
    }

    public static FileObject getView(FileObject controller, PhpBaseElement phpElement, FileObject theme) {
        FileObject view = null;
        if (phpElement instanceof PhpClass.Method) {
            view = getView(controller, getViewFileName(phpElement.getName()), theme);
        }
        return view;
    }

    private static FileObject getView(FileObject controller, String viewName, FileObject theme) {
        File parent = FileUtil.toFile(controller).getParentFile();
        File view = PropertyUtils.resolveFile(parent, String.format(FILE_THEME_VIEW_RELATIVE, theme.getName(), getViewFolderName(controller.getName()), viewName));
        if (!view.isFile()) {
            view = PropertyUtils.resolveFile(parent, String.format(FILE_THEME_VIEW_RELATIVE_2, theme.getName(), getViewFolderName(controller.getName()), viewName));
        }
        if (view.isFile()) {
            return FileUtil.toFileObject(view);
        }
        return null;
    }

    public static boolean isControllerName(String name) {
        return name.endsWith(CakePhpUtils.CONTROLLER_CLASS_SUFFIX);
    }

    public static boolean isControllerFileName(String filename) {
        if (filename.endsWith(CakePhpUtils.CONTROLLER_FILE_SUFIX_2)) {
            return true;
        }
        return filename.endsWith(CakePhpUtils.CONTROLLER_FILE_SUFIX);
    }

    public static boolean isController(FileObject fo) {
        if(fo.isData()
                && isControllerFileName(fo.getName())
                && fo.getParent().getNameExt().equals(DIR_CONTROLLER_2)){
	    return true;
        }
        return fo.isData()
                && isControllerFileName(fo.getName())
                && fo.getParent().getNameExt().equals(DIR_CONTROLLERS);
    }

    public static FileObject getController(FileObject view) {
        File parent = FileUtil.toFile(view).getParentFile();
        File action = PropertyUtils.resolveFile(parent, String.format(FILE_CONTROLLER_RELATIVE, getControllerFileName(parent.getName())));
        if(!action.isFile()){
            action = PropertyUtils.resolveFile(parent, String.format(FILE_CONTROLLER_RELATIVE_2, getControllerFileName(parent.getName())));
        }
        // Theme view file
        if (!action.isFile()) {
            action = PropertyUtils.resolveFile(parent, String.format(FILE_THEME_CONTROLLER_RELATIVE, getControllerFileName(parent.getName())));
        }
        if (!action.isFile()) {
            action = PropertyUtils.resolveFile(parent, String.format(FILE_THEME_CONTROLLER_RELATIVE_2, getControllerFileName(parent.getName())));
        }
        if (action.isFile()) {
            return FileUtil.toFileObject(action);
        }
        return null;
    }

    public static String getActionName(FileObject view) {
        return getActionName(view.getName());
    }

    // unit tests
    static String getActionName(String viewName) {
        return toCamelCase(viewName, true);
    }

    private static String getControllerFileName(String viewName) {
        if (viewName.toLowerCase().equals(viewName)) {
            return viewName + CONTROLLER_FILE_SUFIX;
        }
        return viewName + CONTROLLER_FILE_SUFIX_2; // cake 2.x.x
    }

    // unit tests
    static String getViewFileName(String actionName) {
        return toUnderscore(actionName);
    }

    private static String getViewFolderName(String controllerFileName) {
        if (controllerFileName.toLowerCase().equals(controllerFileName)) {
            return controllerFileName.replace(CONTROLLER_FILE_SUFIX, ""); // NOI18N
        }
        return controllerFileName.replace(CONTROLLER_FILE_SUFIX_2, ""); // NOI18N cake 2.x.x
    }

    // my_posts -> MyPosts or myPosts
    private static String toCamelCase(String underscored, boolean firstLowerCase) {
        StringBuilder sb = new StringBuilder(underscored.length());
        boolean first = firstLowerCase;
        for (String part : underscored.split(Pattern.quote(UNDERSCORE))) { // NOI18N
            if (first) {
                first = false;
                sb.append(part);
            } else {
                sb.append(part.substring(0, 1).toUpperCase());
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    // MyPosts -> my_posts
    private static String toUnderscore(String input) {
        StringBuilder sb = new StringBuilder(2 * input.length());
        for (int i = 0; i < input.length(); ++i) {
            char ch = input.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (i != 0) {
                    sb.append(UNDERSCORE);
                }
                sb.append(Character.toLowerCase(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    public static FileObject createView(FileObject controller, PhpBaseElement phpElement) throws IOException{
        FileObject fo = null;
        if (phpElement instanceof PhpClass.Method) {
            FileObject view = controller.getFileObject("../../" + DIR_VIEWS);
            if (view == null) {
                view = controller.getFileObject("../../" + DIR_VIEW_2);
            }
            if (view != null) {
                fo = view.getFileObject(getViewFolderName(controller.getName()));
                if (fo == null) {
                    fo = view.createFolder(getViewFolderName(controller.getName()));
                }
            }
            if (fo != null) {
                fo = fo.createData(phpElement.getName() + "." + FILE_VIEW_EXT);
            }
        }
        return fo;
    }
    
    /**
     * Get CakePHP version.
     * @param PhpModule phpModule
     * @return String If can't get the version file, return null.
     */
    public static String getCakePhpVersion(PhpModule phpModule){
        FileObject root = CakePhpFrameworkProvider.getCakePhpDirectory(phpModule);
        FileObject cake = root.getFileObject("cake"); // NOI18N
        FileObject version;
        if(cake != null){
            version = root.getFileObject("cake/VERSION.txt"); // NOI18N
        } else {
            version = root.getFileObject("lib/Cake/VERSION.txt"); // NOI18N
        }
        if(version == null){
            return null;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(FileUtil.toFile(version))));
            String str;
            String versionNumber = null;
            while ((str = reader.readLine()) != null) {
                if (!str.contains("//") && !str.equals("")) { // NOI18N
                    str = str.trim();
                    versionNumber = str;
                }
            }
            reader.close();
            return versionNumber;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    public static String[] getCakePhpVersionSplit(PhpModule phpModule){
	    String version = getCakePhpVersion(phpModule);
	    if(version == null){
		    return null;
	    }
	    return version.split("[., -]"); // NOI18N
    }
    
    public static String getCakePhpVersion(PhpModule phpModule, int kind){
        String[] versionArray = getCakePhpVersionSplit(phpModule);
        if (kind < CAKE_VERSION_MAJOR || CAKE_VERSION_NOT_STABLE < kind) {
            return null;
        }
        String version;
        try {
            version = versionArray[kind];
        } catch (ArrayIndexOutOfBoundsException ex) {
            version = null;
        }
        return version;
    }
    
    public static String getCamelCaseName(String name){
	    return toCamelCase(name, false);
    }
    
    /**
     * Create auto completion file
     * - add to the nbproject directory (only CakePHP 2.x)
     * 
     * @return boolean
     * - if create success, return true.
     * 
     * @throws IOException 
     */
    public static boolean createAutoCompletionFile() throws IOException{
        PhpModule pm = PhpModule.inferPhpModule();
        if(!CakePhpFrameworkProvider.getInstance().isInPhpModule(pm)){
            // noting
            return false;
        }
        
        FileObject projectDirectory = pm.getProjectDirectory();
        FileObject nbprojectDirectory = projectDirectory.getFileObject(DIR_NBPROJECT); // NOI18N
        if(nbprojectDirectory == null){
            return false;
        }
        // create a new file for auto completion
        FileObject output = nbprojectDirectory.getFileObject(CAKE_AUTO_COMPLETION_PHP);
        if(output == null){
            output = nbprojectDirectory.createData(CAKE_AUTO_COMPLETION_PHP);
        }
        
        PrintWriter pw = new PrintWriter(output.getOutputStream());
        pw.println("<?php"); // NOI18N
        pw.println("/**"); // NOI18N
        pw.println(" * For CakePHP Component & Model Code Completion"); // NOI18N

        // get models, componets
        List<FileObject> propertyList = getModelFiles();
        propertyList.addAll(getAllComponentFiles());

        // loop add property annotation
        if (propertyList != null) {
            boolean modelFlg = true;
            boolean componentFlg = true;
            List<FileObject> behaviorList = getAllBehaviorFiles();
            for (FileObject property : propertyList) {
                String name = property.getName();
                if(name.endsWith(COMPONENT)){
                    if(componentFlg){
                        pw.println(" * =================================================="); // NOI18N
                        pw.println(" * Component"); // NOI18N
                        pw.println(" * =================================================="); // NOI18N
                        componentFlg = false;
                    }
                    pw.format(PROPERTY_ANNOTATION_PATTERN, name, name.replace(COMPONENT, "")); // NOI18N
                    pw.println();
                }else{
                    if(modelFlg){
                        pw.println(" * =================================================="); // NOI18N
                        pw.println(" * Model"); // NOI18N
                        pw.println(" * =================================================="); // NOI18N
                        modelFlg = false;
                    }
                    pw.format(PROPERTY_ANNOTATION_PATTERN, name, name); // NOI18N
                    pw.println();
                    for(FileObject behavior : behaviorList){
                        pw.format(PROPERTY_ANNOTATION_PATTERN, behavior.getName(), name); // NOI18N
                        pw.println();
                    }
                }
            }
        }
                
        pw.println(" */"); // NOI18N
        pw.println("class Controller{}"); // NOI18N
        
        
        pw.println("/**"); // NOI18N
        pw.println(" * For CakePHP Hepler Code Completion"); // NOI18N

        // get models, componets
        propertyList = getAllHelperFiles();

        // loop add property annotation
        if (propertyList != null) {
            boolean helperFlg = true;
            for (FileObject property : propertyList) {
                String name = property.getName();
                if(name.endsWith(HELPER)){
                    if(helperFlg){
                        pw.println(" * =================================================="); // NOI18N
                        pw.println(" * Helper"); // NOI18N
                        pw.println(" * =================================================="); // NOI18N
                        helperFlg = false;
                    }
                    pw.format(PROPERTY_ANNOTATION_PATTERN, name, name.replace(HELPER, "")); // NOI18N
                    pw.println();
                }
            }
        }
        pw.println(" */"); // NOI18N
        pw.println("class View{}"); // NOI18N
        pw.close();
        UiUtils.open(output, 0);
        return true;
    }
    
    /**
     * Get model files
     * @return List<FileObject>
     */
    public static List<FileObject> getModelFiles(){
        PhpModule pm = PhpModule.inferPhpModule();
        ArrayList<FileObject> modelList = new ArrayList<FileObject>();
        FileObject modelDirectory = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_APP_MODEL); 
        if(modelDirectory == null){
            return null;
        }
        Enumeration<? extends FileObject> children = modelDirectory.getChildren(false);
        while(children.hasMoreElements()){
            FileObject child = children.nextElement();
            String name = child.getName();
            if(!child.isFolder() && !name.startsWith(APP) && !name.equals(EMPTY_FILE)){ // NOI18N
                modelList.add(child);
            }
        }
        return modelList;
    }
    
    /**
     * Get all behaviors
     * @return List<FileObject>
     */
    public static List<FileObject> getAllBehaviorFiles(){
        List<FileObject> behaviorFiles = getCoreBehaviorFiles();
        behaviorFiles.addAll(getAppFiles(BEHAVIOR));
        behaviorFiles.addAll(getPluginFiles(BEHAVIOR));
        return behaviorFiles;
    }

    /**
     * Get Core behaviors
     * @return List<FileObject>
     */
    public static List<FileObject> getCoreBehaviorFiles(){
        PhpModule pm = PhpModule.inferPhpModule();
        FileObject behaviorDirectory = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_LIB_CAKE_MODEL_BEHAVIOR);
        List<FileObject> behaviorList = new ArrayList<FileObject>();
        behaviorList = fileFilter(behaviorDirectory, behaviorList, BEHAVIOR);
        return behaviorList;
    }
    
    /**
     * Get all helpers
     * @return List<FileObject>
     */
    public static List<FileObject> getAllHelperFiles(){
        List<FileObject> helperFiles = getAppFiles(HELPER);
        helperFiles.addAll(getPluginFiles(HELPER));
        return helperFiles;
    }

    /**
     * Get all components
     * @return List<FileObject>
     */
    public static List<FileObject> getAllComponentFiles(){
        List<FileObject> componentFiles = getAppFiles(COMPONENT);
        componentFiles.addAll(getPluginFiles(COMPONENT));
        return componentFiles;
    }
    
    /**
     * Get app files
     * @return List<FileObject>
     */
    public static List<FileObject> getAppFiles(String type){
        PhpModule pm = PhpModule.inferPhpModule();
        FileObject targetDirectory = null;
        if(type.equals(HELPER)){
            targetDirectory = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_APP_VIEW_HELPER);
        }else if(type.equals(COMPONENT)){
            targetDirectory = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_APP_CONTROLLER_COMPONENT);
        }else if(type.equals(BEHAVIOR)){
            targetDirectory = CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_APP_MODEL_BEHAVIOR);
        }
        
        if(targetDirectory == null){
            return null;
        }
        ArrayList<FileObject> addList = new ArrayList<FileObject>();
        
        return fileFilter(targetDirectory, addList, type);
    }
    
    /**
     * Get plugin files
     * @param String type
     * - Component, Behavior, Helper
     * @return List<FileObject>
     * return selected type list 
     */
    public static List<FileObject> getPluginFiles(String type){
        String[] fileTypes = {COMPONENT, BEHAVIOR, HELPER};
        
        PhpModule pm = PhpModule.inferPhpModule();
        List<FileObject> targetList = new ArrayList<FileObject>();
        ArrayList<FileObject> pluginList = new ArrayList<FileObject>();

        if(!Arrays.asList(fileTypes).contains(type)){
            return targetList;
        }
        pluginList.add(CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_APP_PLUGIN));
        pluginList.add(CakePhpFrameworkProvider.getCakePhpDirectory(pm).getFileObject(DIR_PLUGINS));
        
        for(FileObject pluginDirectory : pluginList){
            targetList = fileFilter(pluginDirectory, targetList, type);
        }
        
        return targetList;
    }
    
    /**
     * Files filter
     * @param targetDirectory FileObject
     * @param list List<FileObject>
     * @return List<FileObject>
     */
    private static List<FileObject> fileFilter(FileObject targetDirectory, List<FileObject> list, String type){
        if(targetDirectory == null){
            return list;
        }
        Enumeration<? extends FileObject> children = targetDirectory.getChildren(true);
        while(children.hasMoreElements()){
            FileObject child = children.nextElement();
            String name = child.getName();
            if(!child.isFolder() && name.endsWith(type) && !name.startsWith(APP)){ // NOI18N
                list.add(child);
            }
        }
        return list;
    }
}
