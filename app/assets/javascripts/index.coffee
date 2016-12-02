$ ->
  $("#lookclassinstances").submit (event) ->
    event.preventDefault()
    # send the message to watch the stock
    $.ajax
      url: "/individuals?query=" + $("#dlquery").val()
      dataType: "json"
      context: null
      success: (data) ->
        instances = $("#instances")
        instances.empty()
        for d in data
          panel = $("<div>").addClass("panel").addClass("panel-default")
          panelHead = $("<div>").addClass("panel-heading").text(d['local_name'])
          panelBody = $("<div>").addClass("panel-body")
          for k,v of d["object_property"]
            names = item["name"] for item in v
            panelBody.append($("<span>").text(k + ":" + names).addClass("row"))
          for k,v of d["data_property"]
            panelBody.append($("<span>").text(k + ":" + v).addClass("row"))
          panel.append(panelHead)
          panel.append(panelBody)
          instances.append(panel)
      error: (jqXHR, textStatus, error) ->
        null