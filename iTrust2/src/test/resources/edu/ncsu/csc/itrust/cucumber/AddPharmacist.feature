#Author jshen23

Feature: Add a Pharmacist
	As an Admin
	I want to add a new pharmacist to a pharmacy
	So that the pharmacist can deal with prescriptions

Scenario: Add pharmacist
Given The desired pharmacist does not exist
When I login as an admin
When I navigate to the add user page
When I fill in the values in the add user form with role Pharmacist HCP
Then The pharmacist is created successfully
And The new pharmacist can login

Scenario: Assign pharmacist
Given The desired pharmacist does exist
And The desired Pharmacy does exist
#When I login as an admin
When I navigate to the Pharmacist page
When I choose the pharmacy and pharmacist to assign
Then The pharmacist is assigned to that pharmacy successfully

#Scenario: Fill Prescription
#Given The desired pharmacist has been assigned to a pharmacy
#When I login as an pharmacist
#When I navigate to an unfilled prescription page
#When I choose to fill a prescription
#Then The prescription is filled successfully

#Scenario: Pickup Prescription
#Given The desired pharmacist has been assigned to a pharmacy
#When I login as an pharmacist
#When I navigate to an filled prescription page
#When I click pickup for a prescription
#Then The prescription is removed from the prescription list