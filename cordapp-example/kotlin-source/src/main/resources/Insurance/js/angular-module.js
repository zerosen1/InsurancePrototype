"use strict";

// --------
// WARNING:
// --------

// THIS CODE IS ONLY MADE AVAILABLE FOR DEMONSTRATION PURPOSES AND IS NOT SECURE!
// DO NOT USE IN PRODUCTION!

// FOR SECURITY REASONS, USING A JAVASCRIPT WEB APP HOSTED VIA THE CORDA NODE IS
// NOT THE RECOMMENDED WAY TO INTERFACE WITH CORDA NODES! HOWEVER, FOR THIS
// PRE-ALPHA RELEASE IT'S A USEFUL WAY TO EXPERIMENT WITH THE PLATFORM AS IT ALLOWS
// YOU TO QUICKLY BUILD A UI FOR DEMONSTRATION PURPOSES.

// GOING FORWARD WE RECOMMEND IMPLEMENTING A STANDALONE WEB SERVER THAT AUTHORISES
// VIA THE NODE'S RPC INTERFACE. IN THE COMING WEEKS WE'LL WRITE A TUTORIAL ON
// HOW BEST TO DO THIS.

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/example/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.getInsurances = () => $http.get(apiBaseURL + "Insurances")
        .then((response) => demoApp.Insurances = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getInsurances();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and create Insurance.
    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;

        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createInsuranceEndpoint = `${apiBaseURL}create-Insurance?NRIC=${modalInstance.form.NRIC}&Name=${modalInstance.form.Name}&policyID=${modalInstance.form.policyID}&InsuranceValue=${modalInstance.form.value}`;

            // Create PO and handle success / fail responses.
            $http.put(createInsuranceEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getInsurances();
                },
                (result) => {
                modalInstance.displayMessage(result);
                demoApp.getInsurances();
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behavInsurancer on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create Insurance modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the Insurance.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value);
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});