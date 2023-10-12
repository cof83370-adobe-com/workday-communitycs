$(document).on("dialog-ready", function(){
    addFields();
    $(document).on("click", "._coral-Multifield-remove", function () {
    addFields();  
    });
});
function addFields() {
    const static="workday-community/components/common/relatedinformation", 
          dynamic="workday-community/components/dynamic/relatedinformation";
    if(Granite.author.DialogFrame.currentDialog.editable.type === static
      || Granite.author.DialogFrame.currentDialog.editable.type === dynamic){
    	const collection = document.getElementsByClassName("_coral-Multifield-item").length;
    	if(collection == 0){
        	$("coral-multifield").children("button[coral-multifield-add]").click();
    	}
    }
}