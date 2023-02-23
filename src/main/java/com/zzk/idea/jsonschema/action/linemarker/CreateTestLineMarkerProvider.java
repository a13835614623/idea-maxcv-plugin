package com.zzk.idea.jsonschema.action.linemarker;

import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.GutterName;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.testIntegration.TestFramework;
import com.intellij.testIntegration.TestIntegrationUtils;
import com.intellij.util.FunctionUtil;
import com.zzk.idea.jsonschema.action.test.CreateTestMethodAction;
import com.zzk.idea.jsonschema.constants.Icons;
import com.zzk.idea.jsonschema.constants.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateTestLineMarkerProvider extends LineMarkerProviderDescriptor {

    @Override
    public LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        // This must be an element with a literal expression as a parent
        PsiElement parent = element.getParent();
        if (!(element instanceof PsiIdentifier) || !(parent instanceof PsiMethod)) {
            return null;
        }
        if (TestIntegrationUtils.isTest(element)) {
            return null;
        }
        Optional<TestFramework> testFramework = TestFramework.EXTENSION_NAME.extensions()
                .filter(x -> "JUnit5".equalsIgnoreCase(x.getName()))
                .findFirst();
        if (testFramework.isEmpty()) {
            return null;
        }
        return new ToTestMarkerInfo(element, ((PsiMethod) parent), testFramework.orElseThrow());
    }


    @Override
    public @Nullable("null means disabled") @GutterName String getName() {
        return Message.GENERATE_TEST_METHOD.message();
    }

    private static final class ToTestMarkerInfo extends LineMarkerInfo<PsiElement> {

        private final PsiMethod psiMethod;

        private final TestFramework testFramework;

        private ToTestMarkerInfo(@NotNull PsiElement psiElement, PsiMethod psiMethod, TestFramework testFramework) {
            super(psiElement, psiElement.getTextRange(), testFramework.getIcon(),
                    FunctionUtil.constant(Message.GENERATE_TEST_METHOD.message()),
                    (e, elt) -> {},
                    GutterIconRenderer.Alignment.RIGHT);
            this.psiMethod = psiMethod;
            this.testFramework = testFramework;
        }

        @Override
        public GutterIconRenderer createGutterRenderer() {
            if (myIcon == null) return null;
            return new LineMarkerGutterIconRenderer<>(this) {
                @Override
                public AnAction getClickAction() {
                    return new CreateTestMethodAction(psiMethod,testFramework);
                }
            };
        }
    }
}