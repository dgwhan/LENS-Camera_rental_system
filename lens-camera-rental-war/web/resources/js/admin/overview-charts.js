(function () {
    "use strict";

    var statusChart;
    var activityChart;
    var STATUS_COLORS = {
        pending: "#d97706",
        approved: "#2563eb",
        active: "#059669",
        completed: "#64748b",
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
        return STATUS_COLORS[String(label || "").toLowerCase().trim()] || "#64748b";
    }

    function showEmpty(container) {
        container.innerHTML = '<p class="overview-chart-empty">No data available</p>';
    }

    function createCanvas(container) {
        container.innerHTML = "";
        var canvas = document.createElement("canvas");
        container.appendChild(canvas);
        return canvas;
    }

    function tooltipDefaults() {
        return {
            backgroundColor: "#111111",
            titleColor: "#ffffff",
            bodyColor: "#eeeff3",
            titleFont: { size: 12, weight: "600" },
            bodyFont: { size: 12, weight: "400" },
            padding: 12,
            cornerRadius: 8,
            displayColors: false
        };
    }

    function renderStatusChart() {
        var container = document.getElementById("orderStatusChart");
        var data = readData("overviewStatusJson");

        if (!container || typeof Chart === "undefined") {
            return;
        }

        if (!data.values.length) {
            showEmpty(container);
            return;
        }

        var colors = data.labels.map(getStatusColor);
        var legendBottom = container.clientWidth < 420;

        statusChart = new Chart(createCanvas(container), {
            type: "doughnut",
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: colors,
                    borderColor: "#ffffff",
                    borderWidth: 2,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: "68%",
                animation: {
                    duration: 450
                },
                plugins: {
                    legend: {
                        position: legendBottom ? "bottom" : "right",
                        labels: {
                            usePointStyle: true,
                            pointStyle: "circle",
                            boxWidth: 8,
                            boxHeight: 8,
                            padding: 16,
                            color: "#666666",
                            font: {
                                size: 12,
                                weight: "500"
                            },
                            generateLabels: function (chart) {
                                var dataset = chart.data.datasets[0] || {};
                                var values = dataset.data || [];
                                var fills = dataset.backgroundColor || [];

                                return (chart.data.labels || []).map(function (label, index) {
                                    return {
                                        text: label + "  " + values[index],
                                        fillStyle: fills[index],
                                        strokeStyle: fills[index],
                                        hidden: false,
                                        index: index,
                                        pointStyle: "circle"
                                    };
                                });
                            }
                        }
                    },
                    tooltip: Object.assign(tooltipDefaults(), {
                        callbacks: {
                            title: function () {
                                return "";
                            },
                            label: function (context) {
                                var total = context.dataset.data.reduce(function (sum, value) {
                                    return sum + value;
                                }, 0);
                                var percent = total ? Math.round((context.parsed * 100) / total) : 0;
                                return context.label + "  " + context.parsed + " (" + percent + "%)";
                            }
                        }
                    })
                }
            }
        });
    }

    function renderActivityChart() {
        var container = document.getElementById("orderActivityChart");
        var data = readData("overviewActivityJson");

        if (!container || typeof Chart === "undefined") {
            return;
        }

        if (!data.values.length) {
            showEmpty(container);
            return;
        }

        activityChart = new Chart(createCanvas(container), {
            type: "line",
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    borderColor: "#514FFF",
                    backgroundColor: "rgba(81, 79, 255, 0.12)",
                    borderWidth: 2.5,
                    pointRadius: 0,
                    pointHoverRadius: 5,
                    pointHitRadius: 16,
                    pointBackgroundColor: "#514FFF",
                    pointBorderColor: "#FFFFFF",
                    pointBorderWidth: 2,
                    tension: 0.4,
                    cubicInterpolationMode: "monotone",
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
                animation: {
                    duration: 500,
                    easing: "easeOutQuart"
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: Object.assign(tooltipDefaults(), {
                        callbacks: {
                            label: function (context) {
                                return "Orders  " + context.parsed.y;
                            }
                        }
                    })
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
                            padding: 8,
                            maxRotation: 0,
                            font: {
                                size: 11
                            }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        border: {
                            display: false
                        },
                        grid: {
                            color: "#EEEFF3"
                        },
                        ticks: {
                            color: "#9099A0",
                            precision: 0,
                            padding: 8
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

        Chart.defaults.font.family = getComputedStyle(document.body).fontFamily;
        Chart.defaults.font.size = 12;

        if (statusChart) {
            statusChart.destroy();
            statusChart = null;
        }

        if (activityChart) {
            activityChart.destroy();
            activityChart = null;
        }

        renderStatusChart();
        renderActivityChart();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", render);
    } else {
        render();
    }
})();
