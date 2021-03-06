package org.black.kotlin.diagnostics.netbeans.codefolding;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.PsiCoreCommentImpl;
import com.intellij.psi.util.PsiTreeUtil;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportList;
import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.netbeans.api.editor.fold.Fold;

import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;

import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class KotlinFoldManager implements FoldManager {

    private FoldOperation operation;
    public static final FoldType COMMENT_FOLD_TYPE = new FoldType("/*...*/");
    public static final FoldType IMPORT_LIST_FOLD_TYPE = new FoldType("...");
    public static final FoldType FUNCTION_FOLD_TYPE = new FoldType("{...}");
    private FoldHierarchy hierarchy;
    
    private final Map<TextRange, FoldType> cache = new HashMap<TextRange, FoldType>();
    
    @Override
    public void init(FoldOperation operation) {
        this.operation = operation;
        hierarchy = operation.getHierarchy();
    }

    @Override
    public void initFolds(FoldHierarchyTransaction transaction) {
        try {
            checkCode(transaction);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void insertUpdate(DocumentEvent de, FoldHierarchyTransaction transaction) {
        try {
            checkCode(transaction);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent de, FoldHierarchyTransaction transaction) {
        try {
            checkCode(transaction);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent de, FoldHierarchyTransaction transaction) {
    }

    @Override
    public void removeEmptyNotify(Fold fold) {
    }

    @Override
    public void removeDamagedNotify(Fold fold) {
    }

    @Override
    public void expandNotify(Fold fold) {
    }

    @Override
    public void release() {
    }
    
    private void checkCode(FoldHierarchyTransaction transaction) throws BadLocationException{
        Document document = hierarchy.getComponent().getDocument();
        FileObject file = ProjectUtils.getFileObjectForDocument(document);
        if (file == null || ProjectUtils.getKotlinProjectForFileObject(file) == null){
            return;
        }
        KtFile ktFile = ProjectUtils.getKtFile(document.getText(0, document.getLength()), file);
        
        Collection<? extends PsiElement> elements =
            PsiTreeUtil.findChildrenOfAnyType(ktFile, 
                KtImportList.class, PsiCoreCommentImpl.class, KtNamedFunction.class);
        
        for (PsiElement elem : elements){
            FoldType type = getFoldType(elem);
            int start = getStartOffset(elem, type);
            int end = elem.getTextRange().getEndOffset();
            if (start >= end){
                continue;
            }
            
            if (cache.containsKey(elem.getTextRange())){
                if (cache.get(elem.getTextRange()).equals(type)){
                    continue;
                }
            }
            
            cache.put(elem.getTextRange(), type);
            
            FoldTemplate template = new FoldTemplate(0,0,type.toString());
            
            
            
            operation.addToHierarchy(type, 
                    start, 
                    end, 
                    false, 
                    template, 
                    type.toString(),
                    hierarchy, 
                    transaction);
        }
        
    }
    
    private FoldType getFoldType(PsiElement element){
        if (element instanceof KtImportList){
            return IMPORT_LIST_FOLD_TYPE;
        } else if (element instanceof PsiCoreCommentImpl){
            return COMMENT_FOLD_TYPE;
        } else if (element instanceof KtNamedFunction){
            return FUNCTION_FOLD_TYPE;
        } else return new FoldType("");
    }
    
    private int getStartOffset(PsiElement elem, FoldType type){
        int start = elem.getTextRange().getStartOffset();
        if (type == FUNCTION_FOLD_TYPE){
            String name = elem.getText().split("\\{")[0];
            return start + name.length();
        } else if (type == IMPORT_LIST_FOLD_TYPE){
            return start + "import ".length();
        } else {
            return start;
        }
    }
    
}
