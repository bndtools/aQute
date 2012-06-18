package aqute.eclipse.coffee.editor;

import static aqute.eclipse.coffee.editor.Solarized.*;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.presentation.*;
import org.eclipse.jface.text.rules.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.ui.editors.text.*;
import org.eclipse.ui.texteditor.*;

public class CoffeeConfiguration extends SourceViewerConfiguration {

	final String[]		KEYWORD				= {"true", "false", "null", "this",
			"new", "delete", "typeof", "in", "instanceof", "return", "throw",
			"break", "continue", "debugger", "if", "else", "switch", "for",
			"while", "do", "try", "catch", "finally", "class", "extends",
			"super", "undefined", "then", "unless", "until", "loop", "of",
			"by", "when", "and", "or", "is", "isnt", "not", "yes", "no", "on",
			"off", "false"					};

	// The list of keywords that are reserved by JavaScript, but not used, or
	// are used by CoffeeScript internally. We throw an error when these are
	// encountered, to avoid having a JavaScript error at runtime.

	final String[]		RESERVED			= {"case", "default", "function",
			"var", "void", "with", "const", "let", "enum", "export", "import",
			"native", "__hasProp", "__extends", "__slice", "__bind",
			"__indexOf", "implements", "interface", "let", "package",
			"private", "protected", "public", "static", "yield"};

	final String[]		STRICT_PROSCRIBED	= {"arguments", "eval"};

	final String[]		OPERATORS			= {"->", "+=", "++", "-=", "=",
			"?", "...", "..", "&&", "||", "!", "-", "+", "/=", "*=", "%=",
			"||=", "&&=", "?=", "<<=", ">>=", ">>>=", "&=", "^=", "|=", "&",
			"|", "^", "<<", ">>", ">>>", "==", "!=", "<", ">", "<=", ">=", "*",
			"=>", ".", "?.", ":", "%"			};

	final String		CT_COMMENT			= "__coffee_comment";
	final String		CT_STRING			= "__coffee_string";
	final String		CT_STRING_LITERAL	= "__coffee_string_literal";
	final String		CT_REGEXP			= "__coffee_regexp";
	final String		CT_JAVASCRIPT		= "__coffee_javascript";
	final String[]		CTS					= new String[] {
			IDocument.DEFAULT_CONTENT_TYPE, CT_COMMENT, CT_STRING, CT_REGEXP,
			CT_STRING_LITERAL, CT_JAVASCRIPT};

	final Token			CTT_COMMENT			= new Token(CT_COMMENT);
	final Token			CTT_STRING			= new Token(CT_STRING);
	final Token			CTT_STRING_LITERAL	= new Token(CT_STRING_LITERAL);
	final Token			CTT_JAVASCRIPT		= new Token(CT_JAVASCRIPT);
	final Token			CTT_REGEXP			= new Token(CT_REGEXP);

	Token				T_DEFAULT;
	Token				T_KEYWORD;
	Token				T_CLASSNAME;
	Token				T_RESERVED;
	Token				T_COMMENT;
	Token				T_STRING;
	Token				T_STRING_LITERAL;
	Token				T_STRING_REGEXP;
	Token				T_STRING_REF;
	Token				T_PROSCRIBED;
	Token				T_OPERATOR;
	Token				T_FALSE_OPERATOR;
	Token				T_REGEXP;
	Token				T_JAVASCRIPT;
	Token				T_LABEL;
	Token				T_MEMBER;

	ISharedTextColors	colors;

	public CoffeeConfiguration(ISharedTextColors colors) {
		this.colors = colors;
		T_DEFAULT = token(base03, null, 0);
		T_KEYWORD = token(orange, null, SWT.BOLD);
		T_RESERVED = token(base3, red, TextAttribute.STRIKETHROUGH);
		T_PROSCRIBED = token(yellow, null, SWT.BOLD);
		T_OPERATOR = token(magenta, null, 0);
		T_FALSE_OPERATOR = token(base3, red, SWT.BOLD);;
		T_COMMENT = token(green, null, 0);
		T_STRING = token(violet, null, 0);
		T_STRING_LITERAL = token(violet, null, 0);
		T_STRING_REF = token(violet, null, TextAttribute.UNDERLINE);
		T_JAVASCRIPT = token(yellow, null, 0);
		T_REGEXP = token(violet, null, SWT.BOLD);
		T_CLASSNAME = token(blue, null, SWT.BOLD);
		T_LABEL = token(blue, null, 0);
		T_MEMBER = token(blue, null, 0);
	}

	private Token token(RGB fore, RGB back, int style) {
		return new Token(new TextAttribute(colors.getColor(fore),
				back == null ? null : colors.getColor(back), style));
	}

	public IPresentationReconciler getPresentationReconciler(
			ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		configureReconciler(reconciler, IDocument.DEFAULT_CONTENT_TYPE,
				new SyntaxScanner());
		configureReconciler(reconciler, CT_COMMENT,
				new SimpleScanner(T_COMMENT));
		configureReconciler(reconciler, CT_STRING, new StringScanner());
		configureReconciler(reconciler, CT_STRING_LITERAL, new SimpleScanner(
				T_STRING_LITERAL));
		configureReconciler(reconciler, CT_REGEXP, new SimpleScanner(T_REGEXP,
				new EndOfLineRule("#", T_COMMENT)));
		configureReconciler(reconciler, CT_JAVASCRIPT, new SimpleScanner(
				T_JAVASCRIPT));
		return reconciler;
	}

	private void configureReconciler(PresentationReconciler reconciler,
			String partitionType, ITokenScanner scanner) {
		DefaultDamagerRepairer dr;
		dr = new DefaultDamagerRepairer(scanner);
		reconciler.setDamager(dr, partitionType);
		reconciler.setRepairer(dr, partitionType);
	}

	class SyntaxScanner extends RuleBasedScanner {

		public SyntaxScanner() {
			setDefaultReturnToken(T_DEFAULT);
			KeyWordRule words = new KeyWordRule();
			words.addWords(KEYWORD, T_KEYWORD);
			words.addWords(RESERVED, T_RESERVED);
			words.addWords(STRICT_PROSCRIBED, T_PROSCRIBED);
			OperatorRule operator = new OperatorRule();
			operator.addWords(OPERATORS, T_OPERATOR);
			IRule[] rules = new IRule[] {//
			new EndOfLineRule("#", T_COMMENT), words,//
					operator,//
			};
			setRules(rules);
		}
	}

	class StringScanner extends RuleBasedScanner {

		public StringScanner() {
			setDefaultReturnToken(T_STRING);
			IPredicateRule[] rules = new IPredicateRule[] {new PatternRule(
					"#{", "}", T_STRING_REF, '\\', true),//
			};
			setRules(rules);
		}

	}

	class SimpleScanner extends RuleBasedScanner {

		public SimpleScanner(IToken token, IRule... rules) {
			setDefaultReturnToken(token);
			if (rules.length > 0)
				setRules(rules);
		}

	}

	public IDocumentProvider getDocumentProvider() {
		FileDocumentProvider fdp = new FileDocumentProvider() {
			protected IDocument createDocument(Object element)
					throws CoreException {
				IDocument document = super.createDocument(element);
				if (document != null) {
					RuleBasedPartitionScanner scanner = new RuleBasedPartitionScanner();
					IPredicateRule[] rules = new IPredicateRule[] {
							new MultiLineRule("///", "///", CTT_REGEXP),
							new MultiLineRule("\"\"\"", "\"\"\"", CTT_STRING),
							new PatternRule("\"", "\"", CTT_STRING, '\\', false),//
							new PatternRule("'", "'", CTT_STRING_LITERAL, '\\',
									false),//
							new MultiLineRule("`", "`", CTT_JAVASCRIPT),
							new MultiLineRule("###", "###", CTT_COMMENT),};
					scanner.setPredicateRules(rules);
					FastPartitioner cp = new FastPartitioner(scanner, CTS);
					cp.connect(document);
					document.setDocumentPartitioner(cp);
				}
				return document;
			}
		};
		return fdp;
		// TODO
	}

	class KeyWordRule implements IRule {

		Map<String, IToken>	keyWords	= new HashMap<String, IToken>();

		private boolean isWordStart(char c) {
			return Character.isJavaIdentifierStart(c) || c == '@';
		}

		private boolean isWordPart(char c) {
			return Character.isJavaIdentifierPart(c);
		}

		public IToken evaluate(ICharacterScanner scanner) {
			StringBuffer sb = new StringBuffer();
			int c = scanner.read();

			if (isWordStart((char) c)) {
				do {
					sb.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF && isWordPart((char) c));
				scanner.unread();
				if (c == ':')
					return T_LABEL;

				IToken token = (IToken) keyWords.get(sb.toString());
				if (token != null)
					return token;

				if (sb.length() > 2) {
					if (Character.isUpperCase(sb.charAt(0))
							&& Character.isLowerCase(sb.charAt(1)))
						return T_CLASSNAME;

				}
				if (sb.toString().startsWith("@"))
					return T_MEMBER;

				return T_DEFAULT;
			}
			scanner.unread();

			return Token.UNDEFINED;

		}

		void addWords(String[] words, IToken token) {
			for (int i = 0; i < words.length; ++i) {
				keyWords.put(words[i], token);
			}
		}
	}

	class OperatorRule extends KeyWordRule {

		private boolean isWordStart(char c) {
			return "-><|:+-=!%&^*.?".indexOf(c) >= 0;
		}

		private boolean isWordPart(char c) {
			return "-><|:+-=!%&^*.?".indexOf(c) >= 0;
		}

		public IToken evaluate(ICharacterScanner scanner) {
			StringBuffer sb = new StringBuffer();

			int c = scanner.read();
			if (isWordStart((char) c)) {
				do {
					sb.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF && isWordPart((char) c));

				scanner.unread();

				IToken token = (IToken) keyWords.get(sb.toString());
				if (token != null)
					return token;

				return T_FALSE_OPERATOR;
			}
			scanner.unread();
			return Token.UNDEFINED;

		}
	}

	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new DefaultAnnotationHover();
	}
}
