const MessageTypes = {
    TABLE_DATA: 1,
    LOG_MESSAGE: 2,
    GRAPH_DATA: 3,
    INITIAL_DATA: 4
};

const LogLevels = {
    INFO: "Info",
    WARN: "Warning",
    ERROR: "Error"
};

Object.freeze(MessageTypes);
Object.freeze(LogLevels);


var latestStats = new Map();
var logQues = new Map();
var charts = new Map();
var ids = new Map();
var tables;
var logs;
var graphs;

$(document).ready(function() {
    initializeSocket();
});


function initializeSocket() {
    var socket = new WebSocket("ws://" + location.hostname + ":8001");

    socket.onopen = function(e) {
        console.log("Connection established");
    };
    socket.onmessage = function(event) {
        var jsonData = JSON.parse(event.data);

        if (jsonData.type == MessageTypes.TABLE_DATA) {
            if (getCreatedDetails(jsonData.id).length == 0)
                createTable(jsonData.json, removeSpace(jsonData.id));
            else
                updateTable(jsonData.json, removeSpace(jsonData.id));
        } else if (jsonData.type == MessageTypes.LOG_MESSAGE) {
            logQues.get(removeSpace(jsonData.id)).enqueue(jsonData.json);
        } else if (jsonData.type == MessageTypes.INITIAL_DATA) {
            initializeWebPage(jsonData.json);
        } else if (jsonData.type == MessageTypes.GRAPH_DATA) {
            updateCharts(jsonData.json);
        }


    };

    socket.onclose = function(event) {
        if (event.wasClean) {
            alert(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
        } else {

            alert('Connection died');
        }
    };

    socket.onerror = function(error) {
        alert(`[error] ${error.message}`);
    };

}

function initializeWebPage(data) {
    tables = data.tables
    graphs = data.graphs
    logs = data.logs
    initializeUIElements();

}



function removeSpace(string) {
    return string.replace(/ /g, '')
}

function initializeUIElements() {
    initializeTables();
    initializeLogcat();
    initializeGraphs();
}

function initializeTables() {
    if (tables.length != 0) {

        var parentTable = $('<table/>', { id: "parent_table", class: "table table-borderless" })
        var tableBody = $('<tbody>')
        parentTable.append(tableBody);
        var i = 0;


        while (i < tables.length) {
            if ((i + 1) < tables.length) {

                var row = $('<tr/>')

                var col = $('<td/>', { id: removeSpace(tables[i]) })
                var col2 = $('<td/>', { id: removeSpace(tables[i + 1]) })

                row.append(col)
                row.append(col2)

                var detail = $('<details/>', { id: 'parent_detail-' + removeSpace(tables[i]) })
                detail.append('<summary class="title_summary"><b>' + tables[i] + '</b></summary>')
                var detail1 = $('<details/>', { id: 'parent_detail-' + removeSpace(tables[i + 1]) })
                detail1.append('<summary , class="title_summary" ><b>' + tables[i + 1] + '</b></summary>')
                col.append(detail)
                col2.append(detail1)
                tableBody.append(row)

                i = i + 2;
            } else {
                var row = $('<tr/>')
                var col = $('<td/>', { id: removeSpace(tables[i]) })
                row.append(col)
                var detail = $('<details/>', { id: 'parent_detail-' + removeSpace(tables[i]) })
                detail.append('<summary  class="title_summary"><b>' + tables[i] + '</b></summary>')
                col.append(detail)
                tableBody.append(row)
                break;
            }
        }
        var div = $('<div/>', { class: "background_div" });
        var detail = $('<details/>')
        detail.append('<summary  class="title_summary" style="font-size:34px"><b>Tables</b></summary>')
        $(detail).append('<div id=\"no_data\"class=\"no_data_div \"> No data </div>')

        detail.append(parentTable)
        div.append(detail)
        $('body#body').append(div);
        document.getElementById("parent_table").style.visibility = "hidden"
    }
}

function initializeLogcat() {
    if (logs.length != 0) {
        var div = $('<div/>', { class: "background_div" });
        $('body#body').append(div)
        var logTable = $('<table/>', { id: "log_table", class: "table table-borderless" })
        var detail = $('<details/>')
        detail.append('<summary  class="title_summary" style="font-size:34px"><b>Logs</b></summary>')
        detail.append(logTable)
        div.append(detail)


        for (let i = 0; i < logs.length; i++) {
            var detail = $('<details/>')
            detail.append('<summary class="title_summary"><b>' + logs[i] + '</b></summary>')
            var logCatDiv = $('<div/>', { id: 'log_cat-' + removeSpace(logs[i]), style: "overflow:auto", class: "log_cat_div" })
            detail.append(logCatDiv);
            var row = $('<tr/>')
            row.append(detail)
            logTable.append(row)
            var logQueue = new LogQueue('log_cat-' + removeSpace(logs[i]));
            logQues.set(removeSpace(logs[i]), logQueue)
        }


    }
}

function initializeGraphs() {
    if (graphs.length != 0) {
        var div = $('<div/>', { class: "background_div" });
        var detail = $('<details/>', { id: "graph_detail" })
        div.append(detail)

        var graphDiv = $('<div/>', { id: 'graph-div', class: "graph_div" })
        detail.append(graphDiv);
        $('body#body').append(div)
        detail.append('<summary class="title_summary" style="font-size:34px"><b>Graphs</b></summary>')
        for (let i = 0; i < graphs.length; i++) {
            var secondryDiv = $('<div/>', { class: 'secondary_graph_div' })
            var canvas = $('<canvas/>', { id: removeSpace(graphs[i]), style: 'height: 200px;width: 100%;' })
            secondryDiv.append(canvas)
            graphDiv.append(secondryDiv)
            createGraph(removeSpace(graphs[i]))
        }

    }

}

function createGraph(div_id) {

    const plugin = {
        title: {
            display: true,
            text: 'Title',
            fontColor: "#ffffff",
        },
        id: 'custom_canvas_background_color',
        beforeDraw: (chart) => {
            const ctx = chart.canvas.getContext('2d');
            ctx.save();
            ctx.globalCompositeOperation = 'destination-over';
            ctx.fillStyle = "#464545";
            ctx.fillRect(0, 0, chart.width, chart.height);
            ctx.restore();
        }
    };



    var chart = new Chart(div_id, {
        plugins: [plugin],
        type: 'line',
        data: {
            datasets: [{
                borderColor: "#9dc0f5",
                data: [],
                showLine: true,
                fill: false,
                borderWidth: 1.2,
            }]
        },
        options: {
            plugins: {
                title: {
                    display: true,
                    text: div_id,
                    color: "#ffffff",

                },
                legend: {
                    display: false
                }
            },
            animation: false,
            responsive: true,
            maintainAspectRatio: false,
            elements: {
                point: {
                    radius: 0
                }
            },
            scales: {

                x: {
                    type: 'realtime',
                    realtime: {
                        duration: 300000,
                        delay: 2000,
                    },
                    ticks: {
                        color: "#ffffff",
                    },
                    grid: {
                        borderColor: "#817f7f",
                        color: "#817f7f"
                    },
                    display: true

                },
                y: {
                    ticks: {
                        color: "#ffffff",
                        beginAtZero: false,
                        autoSkip: true,
                        maxTicksLimit: 6
                    },
                    grid: {
                        borderColor: "#817f7f",
                        display: false
                    },
                    display: true,
                    position: 'right',
                    title: {
                        display: true,
                        color: "#ffffff",
                    }
                },
            }
        }
    });
    charts.set(div_id, chart)

}

function createTable(stats, id) {

    Object.entries(stats).forEach((entry) => {
        const [key, value] = entry;
        var detail = $('<details/>', { id: "detail-" + id + key, class: "details" })
        detail.append('<summary style=\"word-break: break-all\" >' + key + '</summary>')
        var table = $('<table class=\"table table-borderless\">', { id: key + 'table' })
        var body = $('<tbody>', { id: key + 'body', class: "inner-table-body" })
        table.append(body)
        detail.append(table)
        Object.entries(value).forEach((entry1) => {
            const [key1, value1] = entry1;
            var row = $('<tr/>', { id: key1 + "row", Text: key1 })

            row.append("<td  id='key-" + key1 + "'>" + key1 + "</td>")
            row.append("<td style=\"word-break: break-all\" id=value-" + key + "-" + key1 + ">" + value1 + "</td>")
            body.append(row)

        })
        $('details#parent_detail-' + id).append(detail)
        addToggleListener(id, key)

    })

    var parent_table = document.getElementById('parent_table')
    if (parent_table.style.visibility === "hidden") {
        document.getElementById('no_data').style.display = "none"
        parent_table.style.visibility = "visible"
    }
}

function addToggleListener(id, key) {
    var detail = document.getElementById("detail-" + id + key)
    detail.addEventListener('toggle', () => {
        if (detail.open) {
            updateStats(id, key);
        }
    })
}

function updateTable(stats, id) {
    var opened = getCreatedDetails(id).filter(detail => detail.hasAttribute("open"))
    latestStats.set(id, stats)

    if (opened.length == 0)
        return

    Object.entries(stats).forEach((entry) => {
            const [key, value] = entry;
            var detail = document.getElementById("detail-" + id + key)
            if (opened.indexOf(detail) != -1) {
                Object.entries(value).forEach((entry1) => {
                    const [key1, value1] = entry1;
                    var row = document.getElementById("value-" + key + "-" + key1)
                    row.innerHTML = value1
                })
                console.log(key + " updated using currently received data")

            }
        }

    )

}

function updateCharts(data) {
    for (let i = 0; i < data.length; i++) {

        var chart = charts.get(removeSpace(data[i].id))

        chart.data.datasets[0].data.push({
            x: data[i].timestamp,
            y: data[i].value
        });
        chart.update();
    }

}

function getCreatedDetails(id) {

    var matches = [];



    var elements = document.getElementById('parent_detail-' + removeSpace(id)).children;
    for (var i = 0; i < elements.length; i++) {

        if (elements[i].id.indexOf('detail-') == 0) {
            matches.push(elements[i]);
        }

    }
    return matches;
}

class LogQueue {
    constructor(log_div) {
        this.logs = [];
        this.isLoggerIsActive = false;
        this.log_div = log_div
    }

    enqueue(message) {
        this.logs.push(message);

        if (!this.isLoggerIsActive) {
            var queue = this
            this.logger = setInterval(function() { activateLogger(queue); }, 500);
            this.isLoggerIsActive = true;

        }
    }


    dequeue() {
        if (this.isEmpty()) {
            clearInterval(this.logger);
            this.isLoggerIsActive = false;
            return null;
        }
        return this.logs.shift();
    }

    isEmpty() {
        return this.logs.length == 0;
    }
}

function activateLogger(queue) {

    var log = queue.dequeue();
    var div = document.getElementById(queue.log_div)
    if (log == null)
        return


    const isScrolledToBottom = div.scrollHeight - div.clientHeight <= div.scrollTop + 1
    const newLogMessage = document.createElement("div")
    newLogMessage.innerHTML = "<p>" + log.time + "&ensp;" + log.logLevel + ":&emsp;" + log.logMessage + "</p>";
    if (log.logLevel == LogLevels.ERROR)
        newLogMessage.style.color = 'red'
    else if (log.logLevel == LogLevels.INFO)
        newLogMessage.style.color = 'lightblue'

    div.appendChild(newLogMessage);

    if (isScrolledToBottom) {
        div.scrollTop = div.scrollHeight - div.clientHeight
    }
    removeLogIfExceedLimit(queue.log_div);




}


function updateStats(id, statsTitle) {

    var updatedStats = latestStats.get(id);
    if (updatedStats == null)
        return

    updateRow(updatedStats[statsTitle], statsTitle);
}

function updateRow(data, statsTitle) {
    Object.entries(data).forEach((entry) => {
        const [key, value] = entry;
        var row = document.getElementById("value-" + statsTitle + "-" + key)
        row.innerHTML = value

    })
    console.log(statsTitle + " updated using stored data")

}

function removeLogIfExceedLimit(div) {
    var len = $("#" + div).children().length;
    if (len >= 80) {
        $("#" + div).find('div').first().remove();
    }
}