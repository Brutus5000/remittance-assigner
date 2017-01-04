package biz.schmidt_it.remittanceassigner.model;

import biz.schmidt_it.remittanceassigner.model.AssignmentAlgorithm;
import biz.schmidt_it.remittanceassigner.model.Invoice;
import javafx.fxml.FXML;
import org.javamoney.moneta.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssigmentAlgorithmTest {

    AssignmentAlgorithm algorithm;
    List<Invoice> input = new ArrayList<>();

    Invoice a = new Invoice("A", Money.of(10000, "EUR"));
    Invoice b = new Invoice("B", Money.of(9800, "EUR"));
    Invoice c = new Invoice("C", Money.of(9700, "EUR"));
    Invoice d = new Invoice("D", Money.of(12000, "EUR"));
    Invoice e = new Invoice("E", Money.of(11900, "EUR"));

    @Before
    public void setUp() {
        Double[] cashbacks = {0.00d, 0.02d, 0.03d};
        algorithm = new AssignmentAlgorithm(Arrays.asList(cashbacks));
    }

    @After
    public void tearDown() {
        input.clear();
    }


    @Test
    public void solveSingleSuccess() {
        input.add(a);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(10000, "EUR"));
        assert result.size() == 1;
        assert result.get(0).getFirst() == a;
    }

    @Test
    public void solveSingleFailTooLow() {
        input.add(a);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(2000, "EUR"));
        assert result.size() == 0;
    }

    @Test
    public void solve_single_fail_too_high() {
        input.add(a);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(12000, "EUR"));
        assert result.size() == 0;
    }

    @Test
    public void solve_double_success() {
        input.add(a); // with 2% cashback
        input.add(b); // with 3% cashback

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(9800 + 9604, "EUR"));
        assert result.size() == 1;
        assert result.get(0).contains(a);
        assert result.get(0).contains(b);
    }

    @Test
    public void solve_double_fail_too_low() {
        input.add(a);
        input.add(b);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(700, "EUR"));
        assert result.size() == 0;
    }


    @Test
    public void solve_double_fail_too_in_between() {
        input.add(a);
        input.add(b);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(9900, "EUR"));
        assert result.size() == 0;
    }

    @Test
    public void solve_double_fail_too_high() {
        input.add(a);
        input.add(b);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(99000, "EUR"));
        assert result.size() == 0;
    }


    @Test
    public void solve_all_cashbacks_as_candidate() {
        input.add(a);
        input.add(b);
        input.add(c);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(10000, "EUR"));
        assert result.size() == 1;
        assert result.get(0).getFirst() == a;
    }


    @Test
    public void solve_multi_complex_cashback() {

        Invoice ia = new Invoice("A", Money.of(500, "EUR"));
        Invoice ib = new Invoice("B", Money.of(200, "EUR"));
        Invoice ic = new Invoice("C", Money.of(100, "EUR"));
        input.add(ia);
        input.add(ib);
        input.add(ic);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(783, "EUR"));
        assert result.size() == 1;
        assert result.get(0).contains(ia);
        assert result.get(0).contains(ib);
        assert result.get(0).contains(ic);
    }


    @Test
    public void solve_multi_4_solutions() {
        input.add(a);
        input.add(b);
        input.add(c);
        input.add(d);
        input.add(e);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(9800 + 11900, "EUR"));
        assert result.size() == 4;
        assert result.get(0).size() == 2;
        assert result.get(0).contains(a);
        assert result.get(0).contains(d);
        assert result.stream().anyMatch(candidate -> candidate.contains(a) && candidate.contains(e));
        assert result.stream().anyMatch(candidate -> candidate.contains(b) && candidate.contains(e));
        assert result.stream().anyMatch(candidate -> candidate.contains(c) && candidate.contains(d));
    }

    @Test
    public void solve_error_result_doubled() {
        input.add(a);
        input.add(c);

        List<AssignmentAlgorithm.Candidate> result = algorithm.solve(input, Money.of(9700, "EUR"));
        assert result.size() == 2;
        assert result.get(0).size() == 1;
        assert result.get(1).size() == 1;
        assert result.stream().anyMatch(candidate -> candidate.contains(a));
        assert result.stream().anyMatch(candidate -> candidate.contains(c));
    }

}