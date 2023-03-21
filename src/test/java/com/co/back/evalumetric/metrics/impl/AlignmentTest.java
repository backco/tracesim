package com.co.back.evalumetric.metrics.impl;

import jaligner.Alignment;
import jaligner.NeedlemanWunsch;
import jaligner.Sequence;
import jaligner.matrix.Matrix;
import jaligner.matrix.MatrixGenerator;
import jaligner.util.SequenceParser;
import jaligner.util.SequenceParserException;
import org.junit.jupiter.api.Test;

public class AlignmentTest {

    @Test
    public void NeedlemanWunschTest () throws SequenceParserException {

	Sequence s1 = SequenceParser.parse("globallocal");
	Sequence s2 = SequenceParser.parse("glocal");
	//System.out.print("3.");
	float  match    = 2;
	float  mismatch = -1;
	Matrix matrix   = MatrixGenerator.generate(match, mismatch);
	float  gap      = 2;
	//System.out.print("4.");
	Alignment alignment = NeedlemanWunsch.align(s1, s2, matrix, gap);
	int       sim       = alignment.getSimilarity();
	double    score     = alignment.calculateScore();
	System.out.println(sim);
	System.out.println(score);
	System.out.println(alignment.getSummary());
    }
}
