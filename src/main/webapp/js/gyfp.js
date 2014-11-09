var gyfp = angular.module("gyfp", []);

gyfp.controller("FileListController", function () {
    this.users = [
        {
            selected: false,
            name: "Greg One",
            email: "greg@greg.greg",
            files: {
                owner: ["a", "b", "c"],
                reader: ["d", "e", "f"],
                writer: ["g", "h"]
            }
        },
        {
            selected: true,
            name: "Greg Two",
            email: "greg2@greg.greg",
            files: {
                owner: [],
                reader: ["f"],
                writer: ["g", "h"]
            }
        },
        {
            name: "Greg Three",
            email: "greg@greg.greg",
            files: {
                owner: ["d", "e", "f", "g", "h"],
                reader: [],
                writer: []
            }
        }
    ];
});