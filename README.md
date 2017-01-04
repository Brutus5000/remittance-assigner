# Remittance Assigner

## Description
When you try to match a customer's remittance against a large number of invoices things get nasty as soon as there is early payment discount involved.

Remittance Assigner is able to find all invoice combinations that match a certain amount under consideration of common cashbacks of 2% and 3%.
Just add the available invoices and their amounts into the table by hand or from clipboard, enter the amount to reconcile and run the algorithm.

Only solutions that reconcile the full amount will be presented.


## How it works
In the first step the algorithm collects all combination of invoices, where the amount to reconcile lies between the 100% and 97% of the invoices sum (due to 3% being the maximum cashback).
For each candidate a brute-force test for all cashback options (0%/2%/3%) checks whether the reconciliation sum is matched exactly. Otherwise the candidate will be ignored.

The brute-force approach is the only way to get all correct answers, but also has a very bad runtime behavior with increasing amount of invoices.

## Technology
* Java 8 is required
* JavaFX is used for the GUI
* javax.money API and the reference implementation org.javamoney.moneta are used for calculations