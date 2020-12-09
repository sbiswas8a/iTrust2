#Author jshen23

Feature: Set default pharmacy
	As an pharmacist
	I want to set my default pharmacy
	So that I can pick up my prescription at a nearby location

Scenario: Set default pharmacy
Given The desired pharmacy does exist
When I login as patient
When I navigate to the Set default pharmacy page
When I select the desired pharmacy
Then The pharmacy is set as default successfully

