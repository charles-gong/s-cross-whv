jQuery(document).ready(function () {



    var monthNames = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
    var HolidayInMonth = [];
    var WeeklyOffsInMonth = [];
    var AvailableDatesInMonth = [];
    var selectedDate;
    var daysJSON;

    var yearChoosen = undefined, monthChoosen = undefined, dayChoosen = undefined;
    var todayDate = new Date();
    var addeddays = $("#IsPaymentAtVac").val();
    todayDate.setDate(todayDate.getDate() - addeddays);
    var days = $("#AvailableDatesAndSlotsJSON").val();
    pushHolidaysWeeklyOffsAndAvailableDays(days);
    var bookingCategoryType = $('#chkdiv input[type=radio]').val();

    function pushHolidaysWeeklyOffsAndAvailableDays(days) {
        HolidayInMonth = [];
        WeeklyOffsInMonth = [];
        AvailableDatesInMonth = [];
        if (days != "null" && days != "") {
            daysJSON = $.parseJSON(days);
            if (monthNames[todayDate.getMonth()] + todayDate.getFullYear() == monthNames[new Date(daysJSON[0]).getMonth()] + new Date(daysJSON[0]).getFullYear()) {
                alert("same month");
            }
            $.each(daysJSON, function (index) {
                var day = daysJSON[index];
                if (day.IsWeekend) {
                    WeeklyOffsInMonth.push(day.Date);
                }
                else if (day.IsHoliday) {
                    HolidayInMonth.push(day.Date);
                }
                else {
                    AvailableDatesInMonth.push(day.Date);
                }
            });
        }
    }

    $('#calendar').fullCalendar({
        header: {
            left: 'prev',
            center: 'title',
            right: 'next'
        },
        defaultView: 'month',
        editable: true,
        allDaySlot: false,
        selectable: true,
        month: parseInt($("#EarliestAllotedMonth").val()) - 1,
        year: parseInt($("#EarliestAllotedYear").val()),
        //lang: 'ru',
        viewDisplay: function getDate(date) {
            if (selectedDate != null) {
                $("[data-date=" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91");
                selectedDate = null;
                var timeBandsDiv = $("#TimeBandsDiv");
                timeBandsDiv.empty();
            }
            var lammCurrentDate = new Date();
            var lammMinDate = new Date(lammCurrentDate.getFullYear(), lammCurrentDate.getMonth(), 1, 0, 0, 0, 0);
            if (date.start <= lammMinDate) {
                $(".fc-button-prev").css("display", "none");
            }
            else {
                $(".fc-button-prev").css("display", "inline-block");
            }
            var calendarDays;
            calendarDays = getCalendarDaysOnMonthChange(date.start.getMonth(), date.start.getFullYear(), bookingCategoryType);

            if (calendarDays != -1) {
                pushHolidaysWeeklyOffsAndAvailableDays(calendarDays);
                showHolidaysWeekendsAndAvaialbleDatesForMonth(date);
            }
            $(".fc-other-month").css("background-color", "none");
        },

        dayRender: function (date, cell) {
        },

        dayClick: function (date, allDay, jsEvent, view) {
            var currentSelectedDate = $.fullCalendar.formatDate(date, 'MM/dd/yyyy');

            // alert($("[data-date=" + $.fullCalendar.formatDate(currentselectedDate, 'yyyy-MM-dd') + "]").css("background-color"));

            //if ($(".fc-day-number").css('background-color') == "rgb(0, 128, 128)") {
            //    $(this).css("background-color", 'rgb(188, 237, 145)');
            //}
            $('.fc-day-number').filter(function () {
                var match = 'rgb(101, 156, 52)'; // match background-color: black
                /*
                    true = keep this element in our wrapped set
                    false = remove this element from our wrapped set
                                                                     */
                return ($(this).css('background-color') == match);

            }).css('background-color', 'rgb(188, 237, 145)').css("color", "black");
            // checking previous date is selected or not.
            if (date < todayDate || date < view.start || date > view.end - 1) {
                return;
            }
            if ($.inArray(currentSelectedDate, HolidayInMonth) > -1) {
                return;
            }
            if ($.inArray(currentSelectedDate, WeeklyOffsInMonth) > -1) {
                return;
            }

            if (AvailableDatesInMonth.length <= 0) {
                pushHolidaysWeeklyOffsAndAvailableDays(days); //added on 24-dec-14
            }

            if ($.inArray(currentSelectedDate, AvailableDatesInMonth) > -1) {
                if (selectedDate != null && selectedDate !== undefined) {
                    if ($.fullCalendar.formatDate(selectedDate, 'MM/dd/yyyy') == currentSelectedDate) {
                        $("[data-date=" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91");
                        selectedDate = null;
                        var timeBandsDiv = $("#TimeBandsDiv");
                        timeBandsDiv.empty();

                    }
                    else {
                        $("[data-date=" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91");
                        selectedDate = date;
                        $('.fc-day-number').filter(function () {
                            var match = 'rgb(188, 237, 145)'; // match background-color: black

                            return ($(this).css('background-color') == match);

                        }).css('background-color', 'rgba(255, 255, 255, 0)').css("color", "black");



                        $(this).css('background-color', "#FF96CA");
                        populateTimeBandsForSelectedDate(currentSelectedDate);

                    }
                }
                else {
                    selectedDate = date;

                    $(this).css('background-color', "#FF96CA");
                    populateTimeBandsForSelectedDate(currentSelectedDate);

                }
            }
            else {
                return;
            }
        },
    });







    $('#chkdiv input[type=radio]').change(function () {

        bookingCategoryType = $(this).val();
        $("#calendar").empty();
        $("#TimeBandsDiv").empty();

        $('#calendar').fullCalendar({
            header: {
                left: 'prev',
                center: 'title',
                right: 'next'
            },
            defaultView: 'month',
            editable: true,
            allDaySlot: false,
            selectable: true,
            //lang: 'ru',
            viewDisplay: function getDate(date) {
                selectedDate = null;
                if (selectedDate != null) {
                    $("[data-date='" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "']").css("background-color", "#BCED91");
                    selectedDate = null;
                    var timeBandsDiv = $("#TimeBandsDiv");
                    timeBandsDiv.empty();
                }
                var lammCurrentDate = new Date();
                var lammMinDate = new Date(lammCurrentDate.getFullYear(), lammCurrentDate.getMonth(), 1, 0, 0, 0, 0);
                if (date.start <= lammMinDate) {
                    $(".fc-button-prev").css("display", "none");
                }
                else {
                    $(".fc-button-prev").css("display", "inline-block");
                }
                var calendarDays;
                if (bookingCategoryType == "General") {

                    calendarDays = getCalendarDaysOnMonthChange(date.start.getMonth(), date.start.getFullYear(), bookingCategoryType);
                } else {

                    calendarDays = getCalendarDaysOnMonthChange(date.start.getMonth(), date.start.getFullYear(), bookingCategoryType);
                }


                if (calendarDays != -1) {
                    pushHolidaysWeeklyOffsAndAvailableDays(calendarDays);
                    showHolidaysWeekendsAndAvaialbleDatesForMonth(date);
                }
                $(".fc-other-month").css("background-color", "none");
            },

            dayRender: function (date, cell) {
            },

            dayClick: function (date, allDay, jsEvent, view) {
                var currentSelectedDate = $.fullCalendar.formatDate(date, 'MM/dd/yyyy');

                // alert($("[data-date=" + $.fullCalendar.formatDate(currentselectedDate, 'yyyy-MM-dd') + "]").css("background-color"));

                //if ($(".fc-day-number").css('background-color') == "rgb(0, 128, 128)") {
                //    $(this).css("background-color", 'rgb(188, 237, 145)');
                //}
                $('.fc-day-number').filter(function () {
                    var match = 'rgb(101, 156, 52)'; // match background-color: black
                    /*
                        true = keep this element in our wrapped set
                        false = remove this element from our wrapped set
                                                                         */
                    return ($(this).css('background-color') == match);

                }).css('background-color', 'rgb(188, 237, 145)').css("color", "black");
                // checking previous date is selected or not.
                if (date < todayDate || date < view.start || date > view.end - 1) {
                    return;
                }
                if ($.inArray(currentSelectedDate, HolidayInMonth) > -1) {
                    return;
                }
                if ($.inArray(currentSelectedDate, WeeklyOffsInMonth) > -1) {
                    return;
                }

                if (AvailableDatesInMonth.length <= 0) {
                    pushHolidaysWeeklyOffsAndAvailableDays(days); //added on 24-dec-14
                }


                if ($.inArray(currentSelectedDate, AvailableDatesInMonth) > -1) {
                    if (selectedDate != null && selectedDate !== undefined) {
                        if ($.fullCalendar.formatDate(selectedDate, 'MM/dd/yyyy') == currentSelectedDate) {
                            $("[data-date=" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91");
                            selectedDate = null;
                            var timeBandsDiv = $("#TimeBandsDiv");
                            timeBandsDiv.empty();

                        }
                        else {
                            $("[data-date=" + $.fullCalendar.formatDate(selectedDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91");
                            selectedDate = date;
                            $('.fc-day-number').filter(function () {
                                var match = 'rgb(188, 237, 145)'; // match background-color: black

                                return ($(this).css('background-color') == match);

                            }).css('background-color', 'rgba(255, 255, 255, 0)').css("color", "black");



                            $(this).css('background-color', "#FF96CA");
                            populateTimeBandsForSelectedDate(currentSelectedDate);

                        }
                    }
                    else {
                        selectedDate = date;

                        $(this).css('background-color', "#FF96CA");
                        populateTimeBandsForSelectedDate(currentSelectedDate);

                    }
                }
                else {
                    return;
                }
            },
        });

    });






    if (yearChoosen != undefined) {

        var x = '' + yearChoosen;
        if (monthChoosen <= 9) {
            x += '-0' + monthChoosen;
        }
        else {
            x += '-' + monthChoosen;
        }

        if (dayChoosen <= 9) {
            x += '-0' + dayChoosen;
        }
        else {
            x += '-' + dayChoosen;
        }


        $('#calendar').fullCalendar('gotoDate', yearChoosen, monthChoosen - 1, dayChoosen);
        //$('td[data-date="'+  x    +'"] div.fc-day-number').trigger('click').css({
        //    "background-color": "#BCED91",
        //    "color" : '#000000'
        //}).end().css("background-color", '#BCED91');
        $('td[data-date="' + x + '"] div.fc-day-number').trigger('click').css({
            "background-color": "#659C34",
            "color": '#000000'
        }).end().css("background-color", 'red');
    }



    function showHolidaysWeekendsAndAvaialbleDatesForMonth(date) {

        //Show Holidays, WeeklyOffs, AvailableSeats
        for (loop = date.start.getTime() ; loop < date.end.getTime() ; loop = loop + (24 * 60 * 60 * 1000)) {
            var currentDate = new Date(loop);
            if ($.inArray($.fullCalendar.formatDate(currentDate, 'MM/dd/yyyy'), HolidayInMonth) > -1) {
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('filter', 'alpha(opacity = 30');
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('color', '#CCCCCC');
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css("background-color", "#FF6A6A");
            }
            if ($.inArray($.fullCalendar.formatDate(currentDate, 'MM/dd/yyyy'), WeeklyOffsInMonth) > -1) {
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('filter', 'alpha(opacity = 30');
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('color', '#CCCCCC');
                $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css("color", "#FF6A6A");
            }
            if ($.inArray($.fullCalendar.formatDate(currentDate, 'MM/dd/yyyy'), AvailableDatesInMonth) > -1) {
                if (currentDate > todayDate) {
                    $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css("background-color", "#BCED91").css("cursor", "pointer");

                }
            }
            $(".fc-other-month.fc-future").css("background-color", "white");
            //else {
            //        $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('filter', 'alpha(opacity = 30');
            //        $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css('color', '#CCCCCC');
            //        $("[data-date=" + $.fullCalendar.formatDate(currentDate, 'yyyy-MM-dd') + "]").css("background-color", "#FF6A6A");
            //}
            //  alert("hai");


        }
    }

    function populateTimeBandsForSelectedDate(selectedDate) {

        var today = new Date();
        var dd = today.getDate();
        var mm = today.getMonth() + 1; //January is 0!
        var yyyy = today.getFullYear();
        var hh = today.getHours();
        var min = today.getMinutes();
        if (dd < 10) { dd = '0' + dd }
        if (mm < 10) { mm = '0' + mm }
        var today = mm + '/' + dd + '/' + yyyy;

        $.each(daysJSON, function (index) {
                if (daysJSON[index].Date == selectedDate) {
                    var timebands = daysJSON[index].TimeBands;
                    var timeBandsDiv = $("#TimeBandsDiv");
                    timeBandsDiv.empty();
                    var bandsTableStr = '<table id=' + "timeBandListTable1" + ' border=' + "1" + ' align=' + "left" + ' cellpadding=' + "0" + ' cellspacing=' + "0" +
                        ' style=' + "background-color: #9EB6FA;border:1px solid #9EB6FA; font-family:Arial, Helvetica, sans-serif; font-size:10px;width:100%" +
                        ' ><tr><th width=' + "50px" + ' style=' + "align:center; background-color: #9EB6FA" + '>' + select + '</th><th width=' + "100px" + ' style=' + "align:center; background-color: #9EB6FA" + '>' + timerange + '</th></tr>';
                    $.each(timebands, function (tmindex) {
                        if (daysJSON[index].Date == today) {
                            var s = timebands[tmindex].EndTime.split(":");
                            //alert(timebands[tmindex].StartTime);
                            if (s[0] >= hh) {

                                if (s[0] == hh) {

                                    if (s[1] >= min) {

                                        bandsTableStr = bandsTableStr + '<tr><td style=' + "text-align:center; background-color: #9EB6FA" + '><input type=' + "radio" + ' name=' + "selectedTimeBand" + ' value = ';
                                        bandsTableStr = bandsTableStr + timebands[tmindex].AllocationId + '></input></td><td style=' + "text-align:center" + '>' + timebands[tmindex].StartTime;
                                        bandsTableStr = bandsTableStr + "-" + timebands[tmindex].EndTime + '</td></tr>';
                                    }
                                }else {
                                    //alert(timebands[index].StartTime);
                                    bandsTableStr = bandsTableStr + '<tr><td style=' + "text-align:center; background-color: #9EB6FA" + '><input type=' + "radio" + ' name=' + "selectedTimeBand" + ' value = ';
                                    bandsTableStr = bandsTableStr + timebands[tmindex].AllocationId + '></input></td><td style=' + "text-align:center" + '>' + timebands[tmindex].StartTime;
                                    bandsTableStr = bandsTableStr + "-" + timebands[tmindex].EndTime + '</td></tr>';
                                }
                            }

                        } else {
                            bandsTableStr = bandsTableStr + '<tr><td style=' + "text-align:center; background-color: #9EB6FA" + '><input type=' + "radio" + ' name=' + "selectedTimeBand" + ' value = ';
                            bandsTableStr = bandsTableStr + timebands[tmindex].AllocationId + '></input></td><td style=' + "text-align:center" + '>' + timebands[tmindex].StartTime;
                            bandsTableStr = bandsTableStr + "-" + timebands[tmindex].EndTime + '</td></tr>';
                        }
                    });
                    bandsTableStr = bandsTableStr + '</table>';
                    timeBandsDiv.append(bandsTableStr);
                    return;
                }
            }
        )
    }


    $(document).on('click', 'input[type="radio"][name="selectedTimeBand"]', function (e) {
        $("#EncryptedSelectedAllocationId").val($(this).val());

    });


    $('#btnConfirm').click(function () {


        if (confirm(alertmsg)) {
            $("#SelectedAllocationId").val('');
            var selectedBand;
            var allTimeBands = $('#TimeBandsDiv').find('td input:radio');
            $.each(allTimeBands, function (i) {
                if (allTimeBands[i].checked == true) {
                    selectedBand = allTimeBands[i].value;
                    $("#SelectedAllocationId").val(selectedBand);
                }
            })
            return true;
        }
        else {
            return false;
        }

    });



    function getCalendarDaysOnMonthChange(month, year, selectedBookingType) {

        // Validating anti forgery tokens in ajax requests
        var token = $('[name=__RequestVerificationToken]').val();
        var headers = {};
        headers["__RequestVerificationToken"] = token;


        var calDays;
        month = month + 1;

        $.ajax({
            url: '../Calendar/GetCalendarDaysOnViewChange',
            contentType: 'application/json; charset=utf-8',
            dataType: 'json',
            headers: headers,
            data: {
                month: month,
                year: year,
                bookingType: selectedBookingType
            },
            async: false,
            success: function (data) {
                if (data.appointmentActionType == "2") {
                    $("input[value = '" + data.appointmentBookingType + "']").prop("checked", true);
                    $("input[name='BookingcategoryType']").prop("disabled", "true");
                }

                if (data.isRedirect) {
                    calDays = data.CalendarDatesOnViewChange;
                    window.location.href = data.redirectUrl;
                }
                else {
                    calDays = data.CalendarDatesOnViewChange;

                    if (data.appointmentAlreadyBlocked != null) {
                        yearChoosen = data.appointmentAlreadyBlocked.Year;
                        monthChoosen = data.appointmentAlreadyBlocked.Month;
                        dayChoosen = data.appointmentAlreadyBlocked.Day;
                    }

                }


            },
            cache: false,
        });

        return calDays;
    }
});



