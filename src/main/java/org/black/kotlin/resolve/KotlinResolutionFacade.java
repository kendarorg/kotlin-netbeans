package org.black.kotlin.resolve;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import java.util.Collection;
import org.black.kotlin.model.KotlinEnvironment;
import org.black.kotlin.project.KotlinProject;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;
import org.jetbrains.kotlin.container.DslKt;

/**
 *
 * @author Александр
 */
public class KotlinResolutionFacade implements ResolutionFacade {
    
    private final KotlinProject kotlinProject;
    private final ComponentProvider componentProvider;
    private final ModuleDescriptor modDesc;
    
    public KotlinResolutionFacade(KotlinProject kotlinProject, ComponentProvider componentProvider, 
            ModuleDescriptor moduleDescriptor){
        this.kotlinProject = kotlinProject;
        this.componentProvider = componentProvider;
        modDesc = moduleDescriptor;
    }

    @Override
    public Project getProject() {
        return KotlinEnvironment.getEnvironment(kotlinProject).getProject();
    }

    @Override
    public BindingContext analyze(KtElement ke, BodyResolveMode brm) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public AnalysisResult analyzeFullyAndGetResult(Collection<? extends KtElement> clctn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DeclarationDescriptor resolveToDescriptor(KtDeclaration kd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ModuleDescriptor getModuleDescriptor() {
        return modDesc;
    }

    @Override
    public <T> T getFrontendService(Class<T> serviceClass) {
        return DslKt.getService(componentProvider, serviceClass);
    }

    @Override
    public <T> T getIdeService(Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getFrontendService(PsiElement pe, Class<T> type) {
        throw new UnsupportedOperationException();    }

    @Override
    public <T> T getFrontendService(ModuleDescriptor md, Class<T> type) {
        throw new UnsupportedOperationException();
    }
    
}
