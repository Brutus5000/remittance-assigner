package biz.schmidt_it.remittanceassigner.model;

import javax.money.MonetaryAmount;
import java.util.*;
import java.util.stream.Collectors;

public class AssignmentAlgorithm {
    public class Candidate {
        private Result assignments;
        private ArrayDeque<Invoice> invoices = new ArrayDeque<>();

        Candidate() {
        }

        private Candidate(ArrayDeque<Invoice> invoices, Result assignments) {
            this.invoices = invoices;
            this.assignments = assignments;
        }

        void addFirst(Invoice invoice) {
            invoices.addFirst(invoice);
        }

        Invoice pollFirst() {
            return invoices.pollFirst();
        }

        Invoice getFirst() {
            return invoices.getFirst();
        }

        public boolean contains(Invoice invoice) {
            return invoices.contains(invoice);
        }

        public int size() {
            return invoices.size();
        }

        @Override
        public Candidate clone() {
            Result r = assignments != null ? assignments.clone() : null;
            Candidate clone = new Candidate(invoices.clone(), r);

            return clone;
        }


        public Result getAssignments() {
            return assignments;
        }

        void setAssignments(Result assignments) {
            this.assignments = assignments;
        }
    }

    public class Result extends ArrayDeque<Assignment> {
        @Override
        public Result clone() {
            return (Result) super.clone();
        }

        MonetaryAmount getRemainingAmount(MonetaryAmount targetAmount) {
            for (Assignment a : this)
                targetAmount = targetAmount.subtract(a.getAppliedAmount());

            return targetAmount;
        }
    }

    private double maxCashback;
    private List<Double> allowedCashbacks;

    public AssignmentAlgorithm(List<Double> allowedCashbacks) {
        this.allowedCashbacks = allowedCashbacks;
        maxCashback = Collections.max(allowedCashbacks);
    }

    public List<Candidate> solve(List<Invoice> invoices, MonetaryAmount targetedAmount) {
        List<Candidate> candidates = getCandidates(invoices, targetedAmount);

        return candidates.stream().filter(
                candidate -> {
                    Result assignments = matchWithCashback(candidate, targetedAmount);

                    if (assignments.size() > 0) {
                        candidate.setAssignments(assignments);
                        return true;
                    } else {
                        return false;
                    }
                }

        )
                .collect(Collectors.toList());
    }

    private List<Candidate> getCandidates(List<Invoice> invoices, MonetaryAmount targetedAmount) {
        Deque<Invoice> remaining = invoices.stream()
                .filter(invoice -> invoice.getAmount().getCurrency() == targetedAmount.getCurrency() && !invoice.getAmount().isZero())
                .collect(Collectors.toCollection(ArrayDeque::new));

        Invoice first;
        List<Candidate> candidateList = new ArrayList<>();

        while (remaining.size() > 0) {
            // by taking the first element per loop, we ensure that each combination is only checked once
            first = remaining.pollFirst();

            if (targetedAmount.subtract(first.getAmount()).isPositive()) {
                // even after deduction of first invoice there is a remaining amount that needs to be reconciled
                List<Candidate> subCandidates = getCandidates(new ArrayList<>(remaining), targetedAmount.subtract(first.getDeductedAmount(maxCashback)));
                subCandidates.add(new Candidate());

                // add the current invoice to the candidates
                final Invoice finalFirst = first;
                subCandidates.forEach(subCandidate -> subCandidate.addFirst(finalFirst));

                candidateList.addAll(subCandidates);
            } else {
                // the first invoice must be able to reconcile the amount or it will be skipped
                if (targetedAmount.subtract(first.getDeductedAmount(maxCashback)).isPositiveOrZero()) {
                    Candidate c = new Candidate();
                    c.addFirst(first);
                    candidateList.add(c);
                }
            }
        }
        return candidateList;
    }

    private Assignment matchWithCashback(Invoice invoice, MonetaryAmount targetedAmount) {
        for (double cashback : allowedCashbacks) {
            if (invoice.getDeductedAmount(cashback).isEqualTo(targetedAmount)) {
                return new Assignment(invoice, cashback);
            }
        }

        return null;
    }

    private Result matchWithCashback(Candidate invoices, MonetaryAmount targetedAmount) {
        Candidate invoiceDeque = invoices.clone();
        Invoice invoice = invoiceDeque.pollFirst();

        if (invoiceDeque.size() == 0) {
            Result result = new Result();
            Assignment match = matchWithCashback(invoice, targetedAmount);
            if (match != null) {
                result.add(match);
                invoiceDeque.addFirst(invoice);
            }
            return result;
        } else {
            for (double cashback : allowedCashbacks) {
                Result result = matchWithCashback(invoiceDeque, targetedAmount.subtract(invoice.getDeductedAmount(cashback)));
                if (result.size() > 0) {

                    result.add(matchWithCashback(invoice, result.getRemainingAmount(targetedAmount)));
                    return result;
                }
            }
        }

        // on fail return empty result
        return new Result();
    }
}
