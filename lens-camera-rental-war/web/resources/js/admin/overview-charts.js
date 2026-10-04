(function () {
    "use strict";

    var statusChart;
    var activityChart;

    var STATUS_COLORS = {
        pending: "#b45309",
        approved: "#2563eb",
        active: "#047857",
        completed: "#475569",
        rejected: "#dc2626",
        cancelled: "#dc2626"
    };

    function readData(id) {
        var node = document.getElementById(id);

        if (!node || !node.value) {
            return { labels: [], values: [] };
        }

        try {
            return JSON.parse(node.value);
        } catch (error) {
            return { labels: [], values: [] };
        }
    }

    function getStatusColor(label) {
        var status = String(label || "").toLowerCase().trim();
        return STATUS_COLORS[status] || "#64748b";
    }

    function renderStatusChart() {
        var container = document.getElementById("orderStatusChart");
        var data = readData("overviewStatusJson");

        if (!container || !data.values.length || typeof Chart === "undefined") {
            return;
        }

        container.innerHTML = "";

        var canvas = document.createElement("canvas");
        container.appendChild(canvas);

        statusChart = new Chart(canvas, {
            type: "doughnut",
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: data.labels.map(getStatusColor),
                    borderWidth: 0,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "68%",
                plugins: {
                    legend: {
                        position: "right",
                        labels: {
                            usePointStyle: true,
                            pointStyle: "circle",
                            boxWidth: 8,
                            padding: 14,
                            color: "#667085",
                            font: {
                                size: 13
                            }
                        }
                    },
                    tooltip: {
                        padding: 10
                    }
                }
            }
        });
    }

    function renderActivityChart() {
        var container = document.getElementById("orderActivityChart");
        var data = readData("overviewActivityJson");

        if (!container || !data.values.length || typeof Chart === "undefined") {
            return;
        }

        container.innerHTML = "";

        var canvas = document.createElement("canvas");
        container.appendChild(canvas);

        activityChart = new Chart(canvas, {
            type: "line",
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    borderColor: "#514FFF",
                    backgroundColor: "rgba(81, 79, 255, 0.06)",
                    borderWidth: 2,
                    pointRadius: 3,
                    pointHoverRadius: 5,
                    pointBackgroundColor: "#514FFF",
                    pointBorderColor: "#FFFFFF",
                    pointBorderWidth: 2,
                    tension: 0.35,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: "index",
                    intersect: false
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        padding: 10,
                        displayColors: false
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        border: {
                            display: false
                        },
                        ticks: {
                            color: "#9099A0",
                            font: {
                                size: 11
                            },
                            maxRotation: 0
                        }
                    },
                    y: {
                        beginAtZero: true,
                        border: {
                            display: false
                        },
                        grid: {
                            color: "#EEF0F3"
                        },
                        ticks: {
                            color: "#9099A0",
                            precision: 0
                        }
                    }
                }
            }
        });
    }

    function render() {
        if (typeof Chart === "undefined") {
            return;
        }

        if (statusChart) {
            statusChart.destroy();
        }

        if (activityChart) {
            activityChart.destroy();
        }

        renderStatusChart();
        renderActivityChart();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", render);
    } else {
        render();
    }

    window.addEventListener("resize", function () {
        if (statusChart) {
            statusChart.resize();
        }

        if (activityChart) {
            activityChart.resize();
        }
    });
})();