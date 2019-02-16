/*!
* jQuery modalBox plugin v1.5.0 <http://code.google.com/p/jquery-modalbox-plugin/>
* @requires jQuery v1.9.0 or later
* is released under the MIT License <http://www.opensource.org/licenses/mit-license.php>
*/
(function(c){var d={minimalTopSpacing:50,draggable:true,disablingClickToClose:false,disablingTheOverlayClickToClose:false,setWidthOfModalLayer:null,customClassName:null,getStaticContentFrom:null,positionLeft:null,positionTop:null,effectType_show_fadingLayer:["fade","fast"],effectType_hide_fadingLayer:["fade","fast"],effectType_show_modalBox:["show"],effectType_hide_modalBox:["hide"],selectorModalbox:"#modalBox",selectorModalBoxBody:"#modalBoxBody",selectorModalBoxBodyContent:".modalBoxBodyContent",selectorModalBoxFaderLayer:"#modalBoxFaderLayer",selectorModalBoxAjaxLoader:"#modalBoxAjaxLoader",selectorModalBoxCloseButton:"#modalBoxCloseButton",selectorModalboxContent:".modalboxContent",selectorModalboxPreCacheContainer:"#modalboxPreCacheContainer",selectorModalBoxImageLink:".modalBoxImageLink",selectorModalBoxImageNoLink:".modalBoxImageNoLink",selectorCloseModalBox:".closeModalBox",selectorAjaxhref:"ajaxhref",setModalboxLayoutContainer_Begin:'<div class="modalboxStyleContainer_surface_left"><div class="modalboxStyleContainer_surface_right"><div class="modalboxStyleContainerContent"><div class="modalBoxBodyContent">',setModalboxLayoutContainer_End:'</div></div></div></div><div class="modalboxStyleContainer_corner_topLeft"><!-- - --></div><div class="modalboxStyleContainer_corner_topRight"><!-- - --></div><div class="modalboxStyleContainer_corner_bottomLeft"><!-- - --></div><div class="modalboxStyleContainer_corner_bottomRight"><!-- - --></div><div class="modalboxStyleContainer_surface_top"><div class="modalboxStyleContainer_surface_body"><!-- - --></div></div><div class="modalboxStyleContainer_surface_bottom"><div class="modalboxStyleContainer_surface_body"><!-- - --></div></div>',localizedStrings:{messageCloseWindow:"Close Window",messageAjaxLoader:"Please wait",errorMessageIfNoDataAvailable:"<strong>No content available!</strong>",errorMessageXMLHttpRequest:'Error: XML-Http-Request Status "500"',errorMessageTextStatusError:"Error: AJAX Request failed",errorMessageImageLoadingFailed:"Error: Image loading failed"},setTypeOfFadingLayer:"black",setStylesOfFadingLayer:{white:"background-color:#fff; filter:alpha(opacity=60); -moz-opacity:0.6; opacity:0.6;",black:"background-color:#000; filter:alpha(opacity=40); -moz-opacity:0.4; opacity:0.4;",transparent:"background-color:transparent;",custom:null},directCall:{source:null,data:null,element:null,image:null},ajax_type:"POST",ajax_contentType:"application/x-www-form-urlencoded; charset=utf-8",callFunctionBeforeShow:function(){return true;},callFunctionAfterShow:function(){},callFunctionBeforeHide:function(){},callFunctionAfterHide:function(){},debug:false,debugOuputMessagePrefix:"[jQuery modalBox plugin] "};try{d=jQuery.extend({},d,modalboxGlobalDefaults);}catch(b){}var a={init:function(i){var i=jQuery.extend({},d,i);var h=jQuery(this);if(i.directCall){if(i.directCall["source"]){f({type:"ajax",source:i.directCall["source"]});}else{if(i.directCall["data"]){f({type:"static",data:i.directCall["data"]});}else{if(i.directCall["element"]){f({type:"static",data:jQuery(i.directCall["element"]).html()});}else{if(i.directCall["image"]){f({type:"image",image:i.directCall["image"],imageLink:i.directCall["imageLink"]});}}}}}var j=false;jQuery(window).resize(function(){j=true;});if(!j&&h.length>0){jQuery(document).on("click","."+h.prop("class"),function(l){g({event:l,element:jQuery(this)});});}function g(p){var p=jQuery.extend({event:null,element:null,doNotOpenModalBoxContent:false,isFormSubmit:false},p||{});if(p.event&&p.element){var t=p.element;var l="";var r="";var s="";var o="";var q="";var m=(typeof(t.attr("href"))!="undefined"?t.attr("href"):"");var n=(typeof(t.attr("rel"))!="undefined"?t.attr("rel"):"");if(t.is("input")){l=t.parents("form").attr("action");r=t.parents("form").serialize();s="ajax";p.isFormSubmit=true;p.event.preventDefault();}else{if(t.find("input[name$='"+i.selectorAjaxhref+"']").length!=0){l=t.find("input[name$='"+i.selectorAjaxhref+"']").val();r="";s="ajax";p.event.preventDefault();}else{if(n.indexOf("ajax:")!=-1){l=n.split("ajax:");l=l[1];r="";s="ajax";p.event.preventDefault();}else{if(a.isImageSource({src:m})){s="image";o=m;checkImageLink=a.extractImageLink({src:m});q=(checkImageLink!=""?checkImageLink:"");p.event.preventDefault();}else{if(a.isImageSource({src:n})){s="image";o=n;checkImageLink=a.extractImageLink({src:n});q=(checkImageLink!=""?checkImageLink:"");p.event.preventDefault();}else{if(t.find(i.selectorModalboxContent).length!=0){l="";r=t.find(i.selectorModalboxContent).html();s="static";p.event.preventDefault();}else{if(i.getStaticContentFrom){l="";r=jQuery(i.getStaticContentFrom).html();s="static";p.event.preventDefault();}else{p.doNotOpenModalBoxContent=true;}}}}}}}if(!p.doNotOpenModalBoxContent){f({type:s,element:t,source:l,data:r,image:o,imageLink:q});}if(p.isFormSubmit){return false;}}}function f(q){var q=jQuery.extend({type:null,element:null,source:null,data:null,image:null,imageLink:null,prepareCustomWidthOfModalBox:"",setModalboxClassName:""},q||{});var m=a.cleanupSelectorName({replaceValue:i.selectorModalBoxImageLink});var x=a.cleanupSelectorName({replaceValue:i.selectorModalBoxImageNoLink});function l(){a.close({callFunctionBeforeHide:i.callFunctionBeforeHide,callFunctionAfterHide:i.callFunctionAfterHide});}function n(){jQuery(!i.disablingClickToClose?(i.selectorModalbox+" "+i.selectorModalBoxBodyContent+" "+i.selectorCloseModalBox+", "+i.selectorModalbox+" "+i.selectorModalBoxCloseButton+" "+i.selectorCloseModalBox+", "+i.selectorModalbox+" "+i.selectorModalBoxImageNoLink):(i.selectorModalbox+" "+i.selectorModalBoxBodyContent+" "+i.selectorCloseModalBox)).off("click").on("click",function(y){l();});}function t(){if(!i.disablingClickToClose&&!i.disablingTheOverlayClickToClose){jQuery(i.selectorModalBoxFaderLayer).off("click").on("click",function(y){l();});}}function v(){a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox});}jQuery(i.selectorModalboxPreCacheContainer).remove();function p(){k();v();}function o(){k({callFunctionAfterShow:i.callFunctionAfterShow});if(i.draggable){a.dragBox();}n();t();v();}if(q.type&&i.callFunctionBeforeShow()){if(q.source){q.source=a.addAjaxUrlParameter({currentURL:q.source});}if(q.element){if(jQuery(q.element).hasClass("large")){q.setModalboxClassName+="large";}else{if(jQuery(q.element).hasClass("medium")){q.setModalboxClassName+="medium";}else{if(jQuery(q.element).hasClass("small")){q.setModalboxClassName+="small";}}}if(jQuery(q.element).hasClass("emphasis")){q.setModalboxClassName+=" emphasis";}}if(q.image){q.setModalboxClassName+="modalBoxSingleImage modalBoxBodyContentImageContainer";}if(i.customClassName){q.setModalboxClassName+=" "+i.customClassName;}if(i.draggable){q.setModalboxClassName+=" modalboxIsDraggable";}if(i.disablingClickToClose){q.setModalboxClassName+=" disablingClickToClose";}if(i.setWidthOfModalLayer){q.prepareCustomWidthOfModalBox+="width:"+parseInt(i.setWidthOfModalLayer)+"px; ";}if(jQuery(i.selectorModalbox).length==0){jQuery("body").append(a.modalboxBuilder({customStyles:'class="'+q.setModalboxClassName+'" style="'+q.prepareCustomWidthOfModalBox+'"'}));}else{a.clean({customClass:q.setModalboxClassName,customWidth:(i.setWidthOfModalLayer?parseInt(i.setWidthOfModalLayer):null)});}var s=jQuery(i.selectorModalbox+" "+i.selectorModalBoxBodyContent);p();switch(q.type){case"static":jQuery(i.selectorModalBoxAjaxLoader).hide();s.html(q.data);o();break;case"ajax":jQuery.ajax({type:i.ajax_type,url:q.source,data:q.data,contentType:i.ajax_contentType,success:function(y,z){jQuery(i.selectorModalBoxAjaxLoader).fadeOut("fast",function(){s.html(y);o();});},error:function(y,A,z){e({ar_XMLHttpRequest:y,ar_textStatus:A,ar_errorThrown:z,targetContainer:i.selectorModalbox+" "+i.selectorModalBoxBodyContent});o();}});break;case"image":var r=jQuery('<img class="modalBoxImagePreload" src="'+q.image+'" />');var u=r.length;var w=0;r.load(function(z,y,B){if(y=="error"){a.debugOutput({msg:"Error / "+B.status+" : "+B.statusText});}else{w++;if(w==u){var A=jQuery(this);jQuery(i.selectorModalBoxAjaxLoader).fadeOut("slow",function(){s.html(A);var C=s.find("img.modalBoxImagePreload");C.removeClass("modalBoxImagePreload").addClass(q.imageLink?"modalBoxImageLoadingSuccessful":"modalBoxImageLoadingSuccessful "+x);if(q.imageLink){C.attr({alt:q.imageLink}).wrap('<a class="'+m+'" href="'+q.imageLink+'" title="'+q.imageLink+'"></a>');jQuery(i.selectorModalbox+" a"+i.selectorModalBoxImageLink).off("click").on("click",function(D){D.preventDefault();a.clean();v();setTimeout(function(){location.href=q.imageLink;},400);});}else{C.attr({alt:i.localizedStrings["messageCloseWindow"],title:i.localizedStrings["messageCloseWindow"]});}o();});}}}).error(function(){a.debugOutput({msg:"Error / "+i.localizedStrings["errorMessageImageLoadingFailed"]});o();});break;}}}function k(l){var l=jQuery.extend({isResized:false,setStyleOfFadingLayer:"",callFunctionAfterShow:null},l||{});if(jQuery(i.selectorModalBoxFaderLayer).length==0){if(i.setTypeOfFadingLayer=="white"){l.setStyleOfFadingLayer=i.setStylesOfFadingLayer["white"];}else{if(i.setTypeOfFadingLayer=="black"){l.setStyleOfFadingLayer=i.setStylesOfFadingLayer["black"];}else{if(i.setTypeOfFadingLayer=="custom"&&i.setStylesOfFadingLayer["custom"]){l.setStyleOfFadingLayer=i.setStylesOfFadingLayer["custom"];}else{l.setStyleOfFadingLayer=i.setStylesOfFadingLayer["transparent"];}}}var n=a.cleanupSelectorName({replaceValue:i.selectorModalBoxFaderLayer});jQuery("body").append('<div id="'+n+'" style="'+l.setStyleOfFadingLayer+'"></div>');var m=jQuery(i.selectorModalBoxFaderLayer);if(i.setTypeOfFadingLayer=="disable"){i.effectType_show_fadingLayer[0]="";}switch(i.effectType_show_fadingLayer[0]){case"fade":m.fadeIn(i.effectType_show_fadingLayer[1],function(){a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox,isResized:l.isResized,callFunctionAfterShow:l.callFunctionAfterShow});});break;default:m.show();a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox,isResized:l.isResized,callFunctionAfterShow:l.callFunctionAfterShow});break;}jQuery(window).resize(function(){if(m.is(":visible")){a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox,isResized:true});}});}else{a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox,isResized:l.isResized,callFunctionAfterShow:l.callFunctionAfterShow});}}function e(m){var m=jQuery.extend({ar_XMLHttpRequest:null,ar_textStatus:null,ar_errorThrown:null,targetContainer:null,ar_enableDebugging:false},m||{});var n=m.ar_XMLHttpRequest;var q=m.ar_textStatus;var o=m.ar_errorThrown;if(n&&q!="error"){if(n.status==403){var p=n.getResponseHeader("Location");if(typeof p!=="undefined"){location.href=p;}}else{if(n.status==500&&m.targetContainer){l({errorMessage:i.localizedStrings["errorMessageXMLHttpRequest"],targetContainer:m.targetContainer});}}if(m.ar_enableDebugging){console.log("XMLHttpRequest.status: "+n.status);}}else{if(q=="error"){if(m.targetContainer){l({errorMessage:i.localizedStrings["errorMessageTextStatusError"],targetContainer:m.targetContainer});}if(m.ar_enableDebugging){console.log("textStatus: "+q);}}else{}}function l(r){var r=jQuery.extend({errorMessage:null,targetContainer:null},r||{});if(r.errorMessage&&r.targetContainer){var s='<div class="simleModalboxErrorBox"><div class="simleModalboxErrorBoxContent">'+r.errorMessage+"</div></div>";jQuery(r.targetContainer).removeAttr("style").html(s);if(jQuery(r.targetContainer).parents(i.selectorModalbox).length>0){jQuery(i.selectorModalBoxAjaxLoader).remove();a.center({positionLeft:i.positionLeft,positionTop:i.positionTop,minimalTopSpacing:i.minimalTopSpacing,effectType_show_modalBox:i.effectType_show_modalBox});}}}}},close:function(e){var e=jQuery.extend({removeOnly:false},e||{});e=jQuery.extend({},d,e);if(e.selectorModalBoxFaderLayer&&e.selectorModalbox){e.callFunctionBeforeHide();var g=jQuery(e.selectorModalBoxFaderLayer+", "+e.selectorModalbox);if(e.removeOnly){f(g);}else{if(e.setTypeOfFadingLayer=="disable"){e.effectType_hide_fadingLayer[0]="";}switch(e.effectType_hide_fadingLayer[0]){case"fade":switch(e.effectType_hide_modalBox[0]){case"fade":jQuery(e.selectorModalbox).fadeOut(e.effectType_hide_modalBox[1],function(){jQuery(e.selectorModalBoxFaderLayer).fadeOut(e.effectType_hide_fadingLayer[1],function(){f(g);});});break;default:jQuery(e.selectorModalbox).hide();jQuery(e.selectorModalBoxFaderLayer).fadeOut(e.effectType_hide_fadingLayer[1],function(){f(g);});break;}break;default:switch(e.effectType_hide_modalBox[0]){case"fade":jQuery(e.selectorModalbox).fadeOut(e.effectType_hide_modalBox[1],function(){f(g);});break;default:f(g);break;}break;}}}function f(h){h.remove();e.callFunctionAfterHide();}},center:function(g){var g=jQuery.extend({isResized:false,callFunctionAfterShow:null},g||{});g=jQuery.extend({},d,g);var e=jQuery(g.selectorModalbox);if(jQuery(g.selectorModalboxPreCacheContainer).length==0&&e.length>0){var f=false;var k="absolute";var j=e.width();var h=e.height();var i=0;var m=parseInt(jQuery(window).width()-j)/2;if(jQuery("body a.modalBoxTopLink").length==0){jQuery("body").prepend('<a class="modalBoxTopLink"></a>');}if(m<=0){m=0;}if(g.positionLeft){m=g.positionLeft+"px";}else{m=m+"px";}if(g.positionTop){i=parseInt(jQuery(window).height()-h);if(i>parseInt(g.positionTop)){k="fixed";}i=g.positionTop+"px";}else{i=parseInt(jQuery(window).height()-h-70)/2;if(i<=0){i=g.minimalTopSpacing+"px";f=true;}else{i=i+"px";k="fixed";}}function l(){if(f&&!e.hasClass("modalboxScrollingSuccessfully")){e.addClass("modalboxScrollingSuccessfully");a.scrollTo();}if(!g.isResized&&g.callFunctionAfterShow){g.callFunctionAfterShow();}}switch(g.effectType_show_modalBox[0]){case"fade":if(e.hasClass("modalboxFadingSuccessfully")){e.css({position:k,left:m,top:i,display:"block",visibility:"visible"});l();}else{e.css({position:k,left:m,top:i,visibility:"visible"}).fadeIn(g.effectType_show_modalBox[1],function(){jQuery(this).addClass("modalboxFadingSuccessfully");l();});}break;default:e.css({position:k,left:m,top:i,display:"block",visibility:"visible"});l();break;}}},clean:function(f){var f=jQuery.extend({customClass:null,customWidth:null},f||{});f=jQuery.extend({},d,f);if(f.selectorModalbox&&f.selectorModalBoxBodyContent){var g=a.cleanupSelectorName({replaceValue:f.selectorModalBoxAjaxLoader});var e=jQuery(f.selectorModalbox);if(f.customClass){e.removeClass(e.attr("class")).addClass(f.customClass);}if(f.customWidth){e.width(f.customWidth);}jQuery(f.selectorModalBoxBodyContent).html('<div id="'+g+'">'+f.localizedStrings["messageAjaxLoader"]+"</div>");}},scrollTo:function(e){var e=jQuery.extend({targetElement:"a.modalBoxTopLink",typeOfAnimation:"swing",animationSpeed:800,callAfterSuccess:function(){}},e||{});if(e.targetElement){jQuery("html").stop().animate({scrollTop:jQuery(e.targetElement).offset().top},e.animationSpeed,e.typeOfAnimation,function(){e.callAfterSuccess();});}},isImageSource:function(e){var e=jQuery.extend({src:null,returnValue:false},e||{});var f=e.src.toLowerCase();if(f.indexOf(".gif")!=-1||f.indexOf(".jpg")!=-1||f.indexOf(".png")!=-1){e.returnValue=true;}return e.returnValue;},extractImageLink:function(e){var e=jQuery.extend({src:null,splitValuePrefix:"link[",splitValueSuffix:"]",returnValue:""},e||{});var f=e.src.toLowerCase();if(f.indexOf(e.splitValuePrefix)!=-1&&f.indexOf(e.splitValueSuffix)!=-1){f=f.split(e.splitValuePrefix);f=f[1].split(e.splitValueSuffix);e.returnValue=f[0];}return e.returnValue;},cleanupSelectorName:function(e){var e=jQuery.extend({replaceValue:""},e||{});var f=e.replaceValue;f=f.replace(/[#]/g,"");f=f.replace(/[.]/g,"");return f;},dragBox:function(f){var f=jQuery.extend({dragObject:null,dragObjectPosX:0,dragObjectPosY:0,documentPosX:0,documentPosY:0},f||{});f=jQuery.extend({},d,f);function e(g){f.dragObject=g;f.dragObjectPosX=(f.documentPosX-f.dragObject.offsetLeft);f.dragObjectPosY=(f.documentPosY-f.dragObject.offsetTop);}jQuery(document).mousemove(function(g){f.documentPosX=g.pageX;f.documentPosY=g.pageY;if(f.dragObject){jQuery(f.dragObject).css({left:(f.documentPosX-f.dragObjectPosX)+"px",top:(f.documentPosY-f.dragObjectPosY)+"px"});}});jQuery(f.selectorModalbox+" .modalboxStyleContainer_surface_top, "+f.selectorModalbox+" .modalboxStyleContainer_surface_bottom").off("mousedown").on("mousedown",function(g){if(g.type=="mousedown"){jQuery(f.selectorModalbox).off("mousemove mouseup").on("mousemove mouseup",function(h){var i=jQuery(this);if(i.is(":visible")){if(h.type=="mousemove"){e(this);}else{if(h.type=="mouseup"){f.dragObject=null;i.off("mousemove");}}}});}});},addAjaxUrlParameter:function(e){var e=jQuery.extend({currentURL:"",addParameterName:"ajaxContent",addParameterValue:"true"},e||{});var g=e.currentURL;if(g.indexOf(e.addParameterName)!=-1){g=g;}else{if(g.indexOf("?")!=-1){var f="&";}else{var f="?";}g=g+f+e.addParameterName+"="+e.addParameterValue;}return g;},precache:function(e){var e=jQuery.extend({},d,e);if(e.selectorModalboxPreCacheContainer){if(jQuery(e.selectorModalboxPreCacheContainer).length==0){var h=a.cleanupSelectorName({replaceValue:e.selectorModalboxPreCacheContainer});var g=a.modalboxBuilder();var f="";f+='<div id="'+h+'" style="position:absolute; left:-9999px; top:-9999px;">';f+=g;f+="</div>";jQuery("body").append(f);jQuery(e.selectorModalbox).show();}}},modalboxBuilder:function(i){var i=jQuery.extend({customStyles:""},i||{});i=jQuery.extend({},d,i);var h=a.cleanupSelectorName({replaceValue:i.selectorModalbox});var f=a.cleanupSelectorName({replaceValue:i.selectorModalBoxBody});var l=a.cleanupSelectorName({replaceValue:i.selectorModalBoxBodyContent});var e=a.cleanupSelectorName({replaceValue:i.selectorModalBoxCloseButton});var k=a.cleanupSelectorName({replaceValue:i.selectorModalBoxAjaxLoader});var g=a.cleanupSelectorName({replaceValue:i.selectorCloseModalBox});var j="";j+='<div id="'+h+'"'+i.customStyles+">";j+='<div id="'+f+'">';j+=i.setModalboxLayoutContainer_Begin;j+='<div class="'+l+'">';j+='<div id="'+k+'">'+i.localizedStrings["messageAjaxLoader"]+"</div>";j+="</div>";j+=i.setModalboxLayoutContainer_End;j+='<div id="'+e+'"><a href="javascript:void(0);" class="'+g+'"><span class="'+g+'">'+i.localizedStrings["messageCloseWindow"]+"</span></a></div>";j+="</div>";j+="</div>";return j;},debugOutput:function(e){var e=jQuery.extend({msg:null},e||{});e=jQuery.extend({},d,e);if(e.debug&&e.msg&&(("console" in window)&&("firebug" in console))){if(typeof(e.msg)=="object"){console.info(e.msg);}else{if(e.msg.trim()!=""){console.debug(e.debugOuputMessagePrefix+e.msg);}else{console.debug(e.msg);}}}}};jQuery.fn.modalBox=function(e){if(a[e]){return a[e].apply(this,Array.prototype.slice.call(arguments,1));}else{if(typeof e==="object"||!e){return a.init.apply(this,arguments);}else{jQuery.error("Method "+e+" does not exist on jQuery.modalBox");}}};jQuery(document).ready(function(){jQuery.fn.modalBox("precache");jQuery(".openmodalbox").modalBox();});})(jQuery);