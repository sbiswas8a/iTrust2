#Author jshen23

Feature: Add a Pharmacy
	As an Admin
	I want to add a new pharmacy
	So that patients can use one of our new facilities

Scenario: Add new pharmacy
Given The desired pharmacy doesn't exist
When I login as admin
When I navigate to the Add Pharmacy page
When I fill in the values in the Add Pharmacy form
Then The pharmacy is created successfully

#Scenario: Delete pharmacy
#Given The desired pharmacy does exist
#When I login as admin
#When I navigate to the Add Pharmacy page
#Then I delete the pharmacy with name <name>

