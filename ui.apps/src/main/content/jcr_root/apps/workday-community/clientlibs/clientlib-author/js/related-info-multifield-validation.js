$(document).on("dialog-ready", function(){
    addFields();
    $(document).on("click", "._coral-Multifield-remove", function () {
    addFields();  
    });
});
function addFields() {
    if(Granite.author.DialogFrame.currentDialog.editable.type === "workday-community/components/common/relatedinformation"
      || Granite.author.DialogFrame.currentDialog.editable.type === "workday-community/components/dynamic/relatedinformation"){
    	const collection = document.getElementsByClassName("_coral-Multifield-item").length;
    	if(collection == 0){
        	$("coral-multifield").children("button[coral-multifield-add]").click();
    	}
    }
}