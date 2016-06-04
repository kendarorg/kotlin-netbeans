package org.black.kotlin.project.structure;

import java.io.IOException;
import javax.swing.JButton;
import org.black.kotlin.project.ui.customizer.KotlinProjectProperties;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Tomas Mysik
 */
public class UpdateProjectImpl implements UpdateImplementation {

    private static final boolean TRANSPARENT_UPDATE = false;//Boolean.getBoolean("j2seproject.transparentUpdate");
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N
    private static final String MINIMUM_ANT_VERSION_ELEMENT = "minimum-ant-version";

    private final Project project;
    private final AntProjectHelper helper;
    private final AuxiliaryConfiguration cfg;
    private boolean alreadyAskedInWriteAccess;
    private Boolean isCurrent;
    private Element cachedElement;

    /**
     * Creates new UpdateHelper
     * @param project
     * @param helper AntProjectHelper
     * @param cfg AuxiliaryConfiguration
     * @param genFileHelper GeneratedFilesHelper
     * @param notifier used to ask user about project update
     */
    UpdateProjectImpl(Project project, AntProjectHelper helper, AuxiliaryConfiguration cfg) {
        assert project != null && helper != null && cfg != null;
        this.project = project;
        this.helper = helper;
        this.cfg = cfg;
    }

    /**
     * Returns true if the project is of current version.
     * @return true if the project is of current version, otherwise false.
     */
    @Override
    public boolean isCurrent () {
        return ProjectManager.mutex().readAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                synchronized (UpdateProjectImpl.this) {
                    if (isCurrent == null) {
                        if ((cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/kotlin-project/1",true) != null)) {
                            isCurrent = Boolean.FALSE;
                        } else {
                            isCurrent = Boolean.TRUE;
                        }
                    }
                    return isCurrent;
                }
            }
        });
    }

    @Override
    public boolean canUpdate () {
        if (TRANSPARENT_UPDATE) {
            return true;
        }
        //Ask just once under a single write access
        if (alreadyAskedInWriteAccess) {
            return false;
        }
        else {
            boolean canUpdate = showUpdateDialog();
            if (!canUpdate) {
                alreadyAskedInWriteAccess = true;
                ProjectManager.mutex().postReadRequest(new Runnable() {
                    @Override
                    public void run() {
                        alreadyAskedInWriteAccess = false;
                    }
                });
            }
            return canUpdate;
        }
    }

    @Override
    public void saveUpdate(final EditableProperties props) throws IOException {
        this.helper.putPrimaryConfigurationData(getUpdatedSharedConfigurationData(),true);
        this.cfg.removeConfigurationFragment("data","http://www.netbeans.org/ns/kotlin-project/1",true); //NOI18N
        ProjectManager.getDefault().saveProject (this.project);
        synchronized(this) {
            this.isCurrent = Boolean.TRUE;
        }
    }

    @Override
    public synchronized Element getUpdatedSharedConfigurationData () {
        if (cachedElement == null) {
            Element  oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/kotlin-project/1",true);    //NOI18N
            if (oldRoot != null) {
                Document doc = oldRoot.getOwnerDocument();
                Element newRoot = doc.createElementNS ("http://www.netbeans.org/ns/kotlin-project/1","data"); //NOI18N
                copyDocument (doc, oldRoot, newRoot);
                Element sourceRoots = doc.createElementNS("http://www.netbeans.org/ns/kotlin-project/1","source-roots");  //NOI18N
                Element root = doc.createElementNS ("http://www.netbeans.org/ns/kotlin-project/1","root");   //NOI18N
                root.setAttribute ("id","src.dir");   //NOI18N
                sourceRoots.appendChild(root);
                newRoot.appendChild (sourceRoots);
                Element testRoots = doc.createElementNS("http://www.netbeans.org/ns/kotlin-project/1","test-roots");  //NOI18N
                root = doc.createElementNS("http://www.netbeans.org/ns/kotlin-project/1","root");   //NOI18N
                root.setAttribute ("id","test.src.dir");   //NOI18N
                testRoots.appendChild (root);
                newRoot.appendChild (testRoots);
                cachedElement = updateMinAntVersion (newRoot, doc);
            } else {
                oldRoot = this.cfg.getConfigurationFragment("data","http://www.netbeans.org/ns/kotlin-project/1",true);    //NOI18N
                if (oldRoot != null) {
                    Document doc = oldRoot.getOwnerDocument();
                    Element newRoot = doc.createElementNS ("http://www.netbeans.org/ns/kotlin-project/1","data"); //NOI18N
                    copyDocument (doc, oldRoot, newRoot);
                    cachedElement = updateMinAntVersion (newRoot, doc);
                    }
                }
            }
        return cachedElement;
    }

    @Override
    public synchronized EditableProperties getUpdatedProjectProperties () {
        EditableProperties cachedProperties = this.helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //The javadoc.additionalparam was not in NB 4.0
        if (cachedProperties.get (KotlinProjectProperties.JAVADOC_ADDITIONALPARAM)==null) {
            cachedProperties.put (KotlinProjectProperties.JAVADOC_ADDITIONALPARAM,"");    //NOI18N
        }
        if (cachedProperties.get ("build.generated.dir")==null) { //NOI18N
            cachedProperties.put ("build.generated.dir","${build.dir}/generated"); //NOI18N
        }
         if (cachedProperties.get ("meta.inf.dir")==null) { //NOI18N
            cachedProperties.put ("meta.inf.dir","${src.dir}/META-INF"); //NOI18N
        }
        return cachedProperties;
    }

    private static void copyDocument (Document doc, Element from, Element to) {
        NodeList nl = from.getChildNodes();
        int length = nl.getLength();
        for (int i=0; i< length; i++) {
            Node node = nl.item (i);
            Node newNode = null;
            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    Element oldElement = (Element) node;
                    newNode = doc.createElementNS("http://www.netbeans.org/ns/kotlin-project/1",oldElement.getTagName());
                    NamedNodeMap m = oldElement.getAttributes();
                    Element newElement = (Element) newNode;
                    for (int index = 0; index < m.getLength(); index++) {
                        Node attr = m.item(index);
                          newElement.setAttribute(attr.getNodeName(), attr.getNodeValue());
                    }
                    copyDocument(doc,oldElement,newElement);
                    break;
                case Node.TEXT_NODE:
                    Text oldText = (Text) node;
                    newNode = doc.createTextNode(oldText.getData());
                    break;
                case Node.COMMENT_NODE:
                    Comment oldComment = (Comment) node;
                    newNode = doc.createComment(oldComment.getData());
                    break;
            }
            if (newNode != null) {
                to.appendChild (newNode);
            }
        }
    }

    private static Element updateMinAntVersion (final Element root, final Document doc) {
        return root;
//        NodeList list = root.getElementsByTagNameNS ("http://www.netbeans.org/ns/kotlin-project/1",MINIMUM_ANT_VERSION_ELEMENT);
//        if (list.getLength() == 1) {
//            Element me = (Element) list.item(0);
//            list = me.getChildNodes();
//            if (list.getLength() == 1) {
//                me.replaceChild (doc.createTextNode("1.6.5"), list.item(0));
//                return root;
//            }
//        }
//        assert false : "Invalid project file"; //NOI18N
//        return root;
    }

    private boolean showUpdateDialog() {
        JButton updateOption = new JButton (NbBundle.getMessage(UpdateProjectImpl.class, "CTL_UpdateOption"));
        updateOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UpdateProjectImpl.class, "AD_UpdateOption"));
        return DialogDisplayer.getDefault().notify(
            new NotifyDescriptor (NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdate", BUILD_NUMBER),
                NbBundle.getMessage(UpdateProjectImpl.class,"TXT_ProjectUpdateTitle"),
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {
                    updateOption,
                    NotifyDescriptor.CANCEL_OPTION
                },
                updateOption)) == updateOption;
    }
}