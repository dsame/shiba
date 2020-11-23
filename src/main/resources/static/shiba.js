// Get value from locale dropdown, and append to URL, submit form for locale selection
$(document).ready(function() {
    $localeSelector = $("#locales");
    $localeSelector.change(function () {
        var selectedOption = $('#locales').val();
        if (selectedOption != ''){
            window.location.replace('?lang=' + selectedOption);
        }
        $("#localeSelector").submit();
    });
});