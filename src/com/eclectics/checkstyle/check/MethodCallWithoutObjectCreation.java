package com.eclectics.checkstyle.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.Check;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.eclectics.checkstyle.util.DetailASTUtil;
import com.eclectics.checkstyle.util.FullIdentComparable;
import com.eclectics.checkstyle.util.JavaClassUtil;

/**
 * You may run into issues if you call a method on a null object. This check
 * gives you the list of those uninitialised variables on which a method has
 * been called. It tries to avoid <code>java.lang.NullPointerException</code>
 * 
 * @author Joe Alex Kimani (kimani.alex@ekenya.co.ke)
 * 
 * @since v1.0, March 05, 2016
 * 
 */
public class MethodCallWithoutObjectCreation extends Check {

	@Override
	public int[] getDefaultTokens() {
		return new int[] { TokenTypes.METHOD_DEF };
	}

	/**
	 * 
	 * @see com.puppycrawl.tools.checkstyle.api.Check#visitToken(com.puppycrawl.tools.checkstyle.api.DetailAST)
	 */
	@Override
	public void visitToken(DetailAST aast) {
		super.visitToken(aast);
		DetailAST slist = aast.findFirstToken(TokenTypes.SLIST);
		List<DetailAST> variables = DetailASTUtil.getDetailASTsForTypeInBranch(
				slist, TokenTypes.VARIABLE_DEF);
		List<DetailAST> uninitializedVars = new ArrayList<DetailAST>();
		for (DetailAST variable : variables) {
			boolean initialized = JavaClassUtil
					.isVariableInitialized(variable);
			if (!initialized)
				uninitializedVars.add(variable);
		}

		for (DetailAST uninitializedVar : uninitializedVars) {
			String uninitializedVarName = uninitializedVar.findFirstToken(
					TokenTypes.IDENT).getText();
			List<DetailAST> siblingsBelow = DetailASTUtil
					.getAllSiblingsBelow(uninitializedVar);
			List<FullIdentComparable> methodCallsFullIdents = new ArrayList<FullIdentComparable>();
			List<FullIdentComparable> assignmentFullIdents = new ArrayList<FullIdentComparable>();
			for (DetailAST siblingBelow : siblingsBelow) {
				getListOfMethodCallsOnVariable(uninitializedVarName,
						siblingBelow, methodCallsFullIdents);
				getListOfVariableAssignment(uninitializedVarName, siblingBelow,
						assignmentFullIdents);
			}
			Collections.sort(methodCallsFullIdents);
			Collections.sort(assignmentFullIdents);
			FullIdentComparable assignFullIdent = null;
			if (assignmentFullIdents.size() > 0) {
				assignFullIdent = assignmentFullIdents.get(0);
			}

			FullIdentComparable methodCallFullIdent = null;
			if (methodCallsFullIdents.size() > 0)
				methodCallFullIdent = methodCallsFullIdents.get(0);
			if (methodCallFullIdent != null
					&& (methodCallFullIdent.compareTo(assignFullIdent) < 0))
				log(uninitializedVar, "Method called without checking null "
						+ methodCallFullIdent.getFullIdent().getText());
		}
	}

	private void getListOfMethodCallsOnVariable(String uninitializedVarName,
			DetailAST siblingBelow,
			List<FullIdentComparable> methodCallsFullIdents) {
		List<DetailAST> dotsInBranch = DetailASTUtil
				.getDetailASTsForTypeInBranch(siblingBelow, TokenTypes.DOT);
		for (DetailAST dotInBranch : dotsInBranch) {
			List<DetailAST> dotChildren = DetailASTUtil
					.getDetailASTsForTypeInChildren(dotInBranch,
							TokenTypes.IDENT);
			if (dotChildren != null) {
				DetailAST dotChild = dotChildren.get(0);
				if (dotChild.getText().equals(uninitializedVarName)) {
					FullIdent fullIdent = FullIdent
							.createFullIdent(dotInBranch);
					methodCallsFullIdents
							.add(new FullIdentComparable(fullIdent));
				}
			}
		}
	}

	private void getListOfVariableAssignment(String uninitializedVarName,
			DetailAST siblingBelow,
			List<FullIdentComparable> assignmentFullIdents) {
		List<DetailAST> assignmentsInBranch = DetailASTUtil
				.getDetailASTsForTypeInBranch(siblingBelow, TokenTypes.ASSIGN);
		for (DetailAST assignInBranch : assignmentsInBranch) {
			DetailAST ident = assignInBranch.findFirstToken(TokenTypes.IDENT);
			if (ident != null) {
				if (ident.getText().equals(uninitializedVarName)) {
					FullIdent fullIdent = FullIdent
							.createFullIdent(assignInBranch);
					assignmentFullIdents
							.add(new FullIdentComparable(fullIdent));
				}
			}
		}
	}
}