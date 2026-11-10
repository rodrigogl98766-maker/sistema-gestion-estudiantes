(function () {
    "use strict";

    document.addEventListener("DOMContentLoaded", function () {
        const body = document.body;
        const sidebarLinks = document.querySelectorAll(".sidebar .nav-link");

        sidebarLinks.forEach(function (link) {
            link.addEventListener("click", function () {
                if (window.matchMedia("(max-width: 980px)").matches) {
                    body.classList.remove("sidebar-open");
                }
            });
        });

        document.addEventListener("keydown", function (event) {
            if (event.key === "Escape") {
                body.classList.remove("sidebar-open");
            }
        });

        window.addEventListener("resize", function () {
            if (!window.matchMedia("(max-width: 980px)").matches) {
                body.classList.remove("sidebar-open");
            }
        });
    });
})();
