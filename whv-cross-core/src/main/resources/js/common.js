jQuery(document).ready(function () {

    $(".appchk").each(function () {
        var loopi = $(this).attr("loopi");
        var isEndorsedChildEnabled = $("#isEndorsedChildEnabledId" + loopi).val();
        var countofnonEndorsedChild = $("input[value='False'].isEndorsedChildEnabledClass").length;

        if (isEndorsedChildEnabled == "True" && countofnonEndorsedChild <= 1) {
            $(this).prop("disabled", true);
        }
    });



    $('#cancel').click(function () {
        //get that logoutForm from document
        // document.getElementById('logoutForm').submit();
        window.location.replace("../Home/Index");
    });

    $('#close').click(function () {
        window.location.replace("../Home/Index");
    });



    $("#EditApplicantForm").submit(function () {
        if ($('#DOB').val() == "") {

        }
        else {
            var returnValue = isValidDate($('#DOB').val(), "yyyy/MM/dd");
            if (!returnValue) {
                $('.errmsg').show();
                msg = "<li>Please enter correct Date format (YYYY/MM/DD) of Date Of Birth</li>";
                $('.errmsg').html(msg);
                return false;
            }
        }
    });

    $("#SearchValidation").submit(function () {
        $(".validation-summary-errors").addClass("validation-summary-valid");
        $(".validation-summary-errors").removeClass("validation-summary-errors");

        if ($('#DOB').val() == "") {
            return true;
        }
        else {
            var returnValue = isValidDate($('#DOB').val(), "yyyy/MM/dd");
            if (!returnValue) {
                $('.errmsg').show();
                msg = "<li>Please enter correct Date format (YYYY/MM/DD) of Date Of Birth</li>";
                $('.errmsg').html(msg);
                return false;
            }
            return returnValue;
        }

        //if (ValidateMandatoryFieldsforValidateForm()) return true;

        //return false;
    });


    function isValidDate(dateStr, format) {
        if (format == null) { format = "YMD"; }
        format = format.toUpperCase();
        if (format.length != 3) { format = "YMD"; }
        if ((format.indexOf("M") == -1) || (format.indexOf("D") == -1) ||
            (format.indexOf("Y") == -1)) { format = "YMD"; }
        if (format.substring(0, 1) == "Y") { // If the year is first
            var reg1 = /^\d{2}(\-|\/|\.)\d{1,2}\1\d{1,2}$/
            var reg2 = /^\d{4}(\-|\/|\.)\d{1,2}\1\d{1,2}$/
        } else if (format.substring(1, 2) == "Y") { // If the year is second
            var reg1 = /^\d{1,2}(\-|\/|\.)\d{2}\1\d{1,2}$/
            var reg2 = /^\d{1,2}(\-|\/|\.)\d{4}\1\d{1,2}$/
        } else { // The year must be third
            var reg1 = /^\d{1,2}(\-|\/|\.)\d{1,2}\1\d{2}$/
            var reg2 = /^\d{1,2}(\-|\/|\.)\d{1,2}\1\d{4}$/
        }
        // If it doesn't conform to the right format (with either a 2 digit year or 4 digit year), fail
        if ((reg1.test(dateStr) == false) && (reg2.test(dateStr) == false)) { return false; }
        var parts = dateStr.split(RegExp.$1); // Split into 3 parts based on what the divider was
        // Check to see if the 3 parts end up making a valid date
        if (format.substring(0, 1) == "M") { var mm = parts[0]; } else
        if (format.substring(1, 2) == "M") { var mm = parts[1]; } else { var mm = parts[2]; }
        if (format.substring(0, 1) == "D") { var dd = parts[0]; } else
        if (format.substring(1, 2) == "D") { var dd = parts[1]; } else { var dd = parts[2]; }
        if (format.substring(0, 1) == "Y") { var yy = parts[0]; } else
        if (format.substring(1, 2) == "Y") { var yy = parts[1]; } else { var yy = parts[2]; }
        if (parseFloat(yy) <= 50) { yy = (parseFloat(yy) + 2000).toString(); }
        if (parseFloat(yy) <= 99) { yy = (parseFloat(yy) + 1900).toString(); }
        var dt = new Date(parseFloat(yy), parseFloat(mm) - 1, parseFloat(dd), 0, 0, 0, 0);
        if (parseFloat(dd) != dt.getDate()) { return false; }
        if (parseFloat(mm) - 1 != dt.getMonth()) { return false; }
        return true;
    }

});

