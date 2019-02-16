
(function ($) {

    /**
     * Polyfill for Object.keys *
     **/
    if (!Object.keys) {
        Object.keys = (function () {
            var hasOwnProperty = Object.prototype.hasOwnProperty,
                hasDontEnumBug = !({ toString: null }).propertyIsEnumerable('toString'),
                dontEnums = [
                    'toString',
                    'toLocaleString',
                    'valueOf',
                    'hasOwnProperty',
                    'isPrototypeOf',
                    'propertyIsEnumerable',
                    'constructor'
                ],
                dontEnumsLength = dontEnums.length;

            return function (obj) {
                if (typeof obj !== 'object' && typeof obj !== 'function' || obj === null) throw new TypeError('Object.keys called on non-object');

                var result = [];

                for (var prop in obj) {
                    if (hasOwnProperty.call(obj, prop)) result.push(prop);
                }

                if (hasDontEnumBug) {
                    for (var i = 0; i < dontEnumsLength; i++) {
                        if (hasOwnProperty.call(obj, dontEnums[i])) result.push(dontEnums[i]);
                    }
                }
                return result;
            }
        })();
    };

    $(function () {

        $(".price-break-down").on("mouseover", function () {
            $(this).siblings(".price-break-down-layer").show();
        });

        $(".price-break-down").on("mouseout", function () {
            $(this).siblings(".price-break-down-layer").hide();
        });

        $('#inline-popups').magnificPopup({
            delegate: 'a',
            removalDelay: 500, //delay removal by X to allow out-animation
            callbacks: {
                beforeOpen: function () {
                    this.st.mainClass = this.st.el.attr('data-effect');
                    loadCart($(this.st.el));
                    OnLoadCheckPremium();
                }
            },
            midClick: true // allow opening popup on middle mouse click. Always set it to true if you don't provide alternative source.
        });



        var $applicantRow = "";

        function loadCart($element) {

            $applicantRow = $element.closest('tr');
            $(".updateCart").removeAttr("disabled");

            var id = $element.attr("data-id");
            $("#encAppId").val(id);
            resetCart(); refresh();
            var loadAlreadyAddedServicesUrl = $("#LoadAlreadyAddedServicesUrl").val();

            $.post(loadAlreadyAddedServicesUrl, { "encApplicantId": id }, function (resp) {

                $.each($('input[type="checkbox"]'), function () {
                    var $self = $(this);
                    var index = Object.keys(resp).indexOf($self.val());
                    if (index != -1) {
                        $('.updateCart').removeAttr("disabled");
                        //$self.attr("checked", "checked");
                        $self.prop("checked", true);
                        var unitSlashIsFree = resp[Object.keys(resp)[index]];
                        var split = unitSlashIsFree.split('/');
                        $self.closest('tr').find('input[type="text"]').val(split[0]);

                        if (split[1] === 'F') {
                            $self.attr("disabled", "disabled");
                        } else {
                            $self.removeAttr("disabled");
                        }
                    }

                    else if ($self.attr('data-checked') === 'True') {
                        $('.updateCart').removeAttr("disabled");
                        $self.trigger('click');
                    }

                    if ($self.attr('data-disabled') == 'True') {
                        $self.attr("disabled", "disabled");
                    }
                });

                updatePrice();
            });
        };


        $(".chkvas").on('click', function () {
            GetFreeServices(this);
        });


        function GetFreeServices(Obj) {

            var $self = $(Obj);
            var freeservicesUrl = $("#FreeServicesUrl").val();
            var ajaxSettings = {
                dataType: 'json',
                type: "POST",
                url: freeservicesUrl,
                data: { "encryptedServiceId": $self.val() },
                async: false
            }

            if ($self.is(":checked")) {

                $.ajax(ajaxSettings).done(function (resp) {
                    refresh(resp, $self.val(), true);
                    updatePrice();
                });
            }
            else {
                $.ajax(ajaxSettings).done(function (resp) {
                    refresh(resp, $self.val(), false);
                    updatePrice();
                });
            }
        }

        function GetFreeServices_Load(Obj) {

            var $self = $(Obj);
            var freeservicesUrl = $("#FreeServicesUrl").val();
            var ajaxSettings = {
                dataType: 'json',
                type: "POST",
                url: freeservicesUrl,
                data: { "encryptedServiceId": $self.val() },
                async: false
            }

            if ($self.attr("data-checked") == 'True') {

                $.ajax(ajaxSettings).done(function (resp) {
                    refresh(resp, $self.val(), true);
                    updatePrice();
                });
            }
            else {
                $.ajax(ajaxSettings).done(function (resp) {
                    refresh(resp, $self.val(), false);
                    updatePrice();
                });
            }
        }
        function OnLoadCheckPremium() {
            $(".chkvas").each(function (e) {
                if ($(this).attr('data-checked') == 'True') {
                    GetFreeServices_Load(this);
                }
            });
        }

        function refresh(resp, parentId, checked) {

            if (resp) {
                if (resp.length == 0) {
                    return;
                }

                $.each($(".chkvas"), function () {
                    var $self = $(this);

                    $.each(resp, function (i, e) {
                        if (e.EncFreeServiceId === $self.val()) {

                            if (checked || $self.attr('data-checked') === 'True') {
                                //$self.attr("checked", "checked");
                                $self.prop( "checked", true );
                                $self.attr("disabled", "disabled");

                            } else {
                                $self.removeAttr("checked");
                                $self.removeAttr("disabled");
                            }
                        }
                    });
                });
            }
        }

        function resetCart() {
            $('input[type="checkbox"]').removeAttr("checked");
            $('input[type="checkbox"]').removeAttr("disabled");
            $('input[type="text"]').val("1").attr('disabled');
            $(".updateCart").removeAttr("disabled");
            updatePrice();
        }

        function updatePrice() {

            var totalPayable = 0.0;

            $.each($("div#test-popup > table > tbody > tr"), function () {
                var $row = $(this);
                var $chk = $($row.find('input[type="checkbox"]'));
                var ischkChecked = $chk.is(":checked");
                var isMandatory = $chk.attr("data-disabled") === "True";

                if (ischkChecked && (!$($row.find('input[type="checkbox"]')).is(":disabled") || isMandatory)) {

                    var srvCst = parseFloat($($row.find('td.sub-total-cntr span.prc-ctnr span.sub-total span.serviceCostWithTax')).html().trim());
                    var subTotal = srvCst;
                    totalPayable += subTotal;

                } else if (ischkChecked && $($row.find('input[type="checkbox"]')).is(":disabled") && !isMandatory) {
                    $($row.find('span.sub-total')).addClass("strikeThrough");
                    $($row.find('input[type="text"]')).val(1);
                    $($row.find('input[type="text"]')).attr("disabled", "disabled");
                    $($row.find('td.sub-total-cntr span.prc-ctnr')).show();
                } else {
                    $($row.find('input[type="text"]')).attr("disabled", "disabled").val('1');
                    $($row.find('span.sub-total')).removeClass("strikeThrough");
                }
            });

            $(".total-payable").html(totalPayable.toFixed(2));
        }

        $("input[type='text'].qty").on('change', function () {
            updatePrice();
        });


        $(".updateCart").on("click", function () {

            var qty = [];

            $(this).attr("disabled", "disabled");

            $.each($("input[type='text']"), function () {
                var $self = $(this);
                if ($self.closest('tr').find('input[type="checkbox"]').is(":checked")) {
                    qty.push($self.val());
                }
            });


            var s = "";
            $.each($(".chkvas"), function () {
                var $self = $(this);
                if ($self.is(":checked")) {
                    s += $self.val() + "!";
                }
            });

            var x = s.split('');
            x.splice(-1, 1);

            var requestMdl = new updateCartMdl($("#encAppId").val(), x.join(''), qty);
            var saveCartUrl = $("#saveCartUrl").val();
            $.ajax({
                url: saveCartUrl,
                dataType: "json",
                traditional: true,
                type: "POST",
                data: requestMdl,
                success: function (resp) {

                    var v = resp.trim();
                    var split = v.split('/');

                    if (split.length > 1) {
                        var t = split[split.length - 1];
                        $applicantRow.find('span.ts > span.ct-desc').html(split[0]);
                        $applicantRow.find('span.ts > span.booked-cost-of-cart').html(t);
                    } else {
                        $applicantRow.find('span.ts > span.ct-desc').html(resp);
                        $applicantRow.find('span.ts > span.booked-cost-of-cart').html('');
                    }

                    var total = updateTotalPayable($('span.ts'));
                    $("#totalPayableGrp").html(total);
                    location.reload();
                },
                error: function (xmlHttpRequest, textStatus, errorThrown) {
                    //
                }
            });

        });


        function updateCartMdl(encApplicantId, encSelectedServices, qty) {

            this.EncApplicantId = encApplicantId;
            this.EncSelectedServices = encSelectedServices;
            this.Qty = qty;
        }

        function updateTotalPayable($collection) {
            var total = 0.00;
            $collection.each(function () {

                var t = $(this).find("span.booked-cost-of-cart").html();

                try {
                    var ttl = parseFloat(parseFloat(t.trim()).toFixed(2));
                    if (!isNaN(ttl)) {
                        total += ttl;
                    }

                } catch (e) {

                }

            });

            return total.toFixed(2);
        }


    });

}(jQuery));


