$ ->
  $("#lookclassinstances").submit (event) ->
    event.preventDefault()
    # send the message to watch the stock
    queryInstances($("#dlquery").val())
  ajaxServer(decodeURIComponent(getParams()["query"])) if getParams()["query"] 

queryInstances = (query) ->
  window.location.href ='/?query='+encodeURIComponent(query)

ajaxServer = (query) -> 
  console.log(query)
  $.ajax
    url: "/individuals?query=" + encodeURIComponent(query)
    dataType: "json"
    context: null
    success: (data) ->
      instances = $("#instances")
      instances.empty()
      for d in data
        classes = links = $("<span>") \
          .append($("<a>").text(imar["name"]).attr "href", '/?query='+encodeURIComponent(imar["name"])) \
          for imar in d["class_property"]
        panel = $("<div>").addClass("panel").addClass("panel-default")
        panelHead = $("<div>").addClass("panel-heading") \
          .append($("<span>").append($("<a>").text(d['local_name']).attr "href", "/?query="+encodeURIComponent("{" + d['local_name'] + "}"))) \
          .append(':').append(classes)
        panelBody = $("<div>").addClass("panel-body")
        for k,v of d["object_property"]
          panelRow = $("<span>").append(k + " : ").addClass("row")
          for item in v
            link = $("<span>") \
              .append($("<a>").text(item["name"]).attr "href", "/?query="+encodeURIComponent("{" + item["name"] + "}")) \
              .append($("<a>").attr("href","/?query="+encodeURIComponent(k + " some {" + item["name"] + "}")).append($("<button>").text("alt")))
            panelRow.append(link)
            console.log(link)
          panelBody.append(panelRow)
        for k,v of d["data_property"]
          panelBody.append($("<span>").text(k + ":" + v).addClass("row"))
        panel.append(panelHead)
        panel.append(panelBody)
        instances.append(panel)
      error: (jqXHR, textStatus, error) ->
        null

getParams = ->
  query = window.location.search.substring(1)
  raw_vars = query.split("&")

  params = {}

  for v in raw_vars
    [key, val] = v.split("=")
    params[key] = decodeURIComponent(val)

  params

console.log(getParams())