package com.eclectics.checkstyle.check;

import java.util.Arrays;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Sometimes you may want to avoid the usage of <code>error()</code>,
 * <code>warning()</code> methods on Log4J (for that matter any other) logger
 * in the application so that those concerns could be taken care of by
 * application framework itself. The reason may be - these methods get logged
 * even in production environment and you may want to use them judiciously. In
 * those cases this check is useful.
 * 
  * @author Joe Alex Kimani (kimani.alex@ekenya.co.ke)
 * 
 * @since v1.0, March 22, 2016
 * 
 */
public final class IllegalLoggerUsageCheck extends Check {

	List<String> illegalLoggerUsage;

	/** Creates new instance of the check. */
	public IllegalLoggerUsageCheck() {
		super();
	}

	public void executeMustFixCheck(DetailAST aDetailAST) {

	}

	/** {@inheritDoc} */
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.METHOD_CALL };
	}

	/** {@inheritDoc} */
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	/** {@inheritDoc} */
	public void visitToken(DetailAST aDetailAST) {
		DetailAST dotDetailAST = aDetailAST.findFirstToken(TokenTypes.DOT);
		if (dotDetailAST == null) {
			return;
		}
		final FullIdent ident;

		ident = FullIdent.createFullIdent(dotDetailAST);

		if (illegalLoggerUsage.contains(ident.getText().substring(
				ident.getText().lastIndexOf(".") + 1))) {
			log(aDetailAST,
					"Please check the use of the method for LOGGING -> "
							+ ident.getText()
							+ " \n Method valid for FRAMEWORK"
							+ "\n Next sibling is on line @"
							+ aDetailAST.getNextSibling().getLine()
							+ "\n Next sibling is on Column @"
							+ aDetailAST.getNextSibling().getColumn());
		}
	}

	public void setIllegalLoggerUsage(String illegalLoggerUsageStr) {
		this.illegalLoggerUsage = Arrays.asList(illegalLoggerUsageStr
				.split(","));
	}
}