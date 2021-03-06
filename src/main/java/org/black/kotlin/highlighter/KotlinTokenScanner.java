package org.black.kotlin.highlighter;

import com.intellij.psi.PsiElement;
import java.util.ArrayList;
import java.util.List;
import org.black.kotlin.builder.KotlinPsiManager;
import org.black.kotlin.highlighter.netbeans.KotlinToken;
import org.black.kotlin.highlighter.netbeans.KotlinTokenId;
import org.jetbrains.kotlin.psi.KtFile;
import org.netbeans.spi.lexer.LexerInput;

/**
 * KotlinTokenScanner parses kotlin code for tokens
 *
 * @author Александр
 *
 */
public final class KotlinTokenScanner {

    private final KotlinTokensFactory kotlinTokensFactory;

    private final KtFile ktFile;
    private List<KotlinToken<KotlinTokenId>> kotlinTokens;
    private int offset = 0;
    private int tokensNumber = 0;
    private final LexerInput input;

    /**
     * Class constructor
     *
     * @param input code that must be parsed
     */
    public KotlinTokenScanner(LexerInput input) {
        kotlinTokensFactory = new KotlinTokensFactory();
        this.input = input;
        ktFile = KotlinPsiManager.INSTANCE.getParsedKtFileForSyntaxHighlighting(getTextToParse());
        createListOfKotlinTokens();
    }

    private String getTextToParse() {
        StringBuilder builder = new StringBuilder("");

        int character;

        do {
            character = input.read();
            builder.append((char) character);
        } while (character != LexerInput.EOF);

        input.backup(input.readLengthEOF());

        return builder.toString();
    }

    /**
     * This method creates an ArrayList of tokens from the parsed ktFile.
     */
    private void createListOfKotlinTokens() {
        kotlinTokens = new ArrayList<KotlinToken<KotlinTokenId>>();
        PsiElement lastElement;
        for (;;) {

            lastElement = ktFile.findElementAt(offset);

            if (lastElement != null) {
                offset = lastElement.getTextRange().getEndOffset();
                TokenType tokenType = kotlinTokensFactory.getToken(lastElement);

                kotlinTokens.add(new KotlinToken<KotlinTokenId>(
                        new KotlinTokenId(tokenType.name(), tokenType.name(), tokenType.getId()),
                        lastElement.getText(), tokenType));
                tokensNumber = kotlinTokens.size();
            } else {
                kotlinTokens.add(new KotlinToken<KotlinTokenId>(
                        new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                        TokenType.EOF));
                tokensNumber = kotlinTokens.size();
                break;
            }

        }

    }

    /**
     * Returns the next token from the kotlinTokens ArrayList.
     *
     * @return {@link KotlinToken}
     */
    public KotlinToken<KotlinTokenId> getNextToken() {

        KotlinToken<KotlinTokenId> ktToken;

        if (tokensNumber > 0) {
            ktToken = kotlinTokens.get(kotlinTokens.size() - tokensNumber--);
            if (ktToken != null) {
                int tokenLength = ktToken.length();
                while (tokenLength > 0) {
                    input.read();
                    tokenLength--;
                }
            }
            return ktToken;
        } else {
            input.read();
            return new KotlinToken<KotlinTokenId>(
                    new KotlinTokenId(TokenType.EOF.name(), TokenType.EOF.name(), 7), "",
                    TokenType.EOF);
        }

    }

}
