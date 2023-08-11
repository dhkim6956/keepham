import React from "react";

interface DateLabelProps {
  date: Date;
}

const DateLabel: React.FC<DateLabelProps> = (props) => {
  const year = new Date(props.date).getFullYear();
  const month = ("00" + (new Date(props.date).getMonth() + 1)).slice(-2);
  const day = ("00" + new Date(props.date).getDate()).slice(-2);

  return (
    <span className="fs-small fw-light">
      {year}-{month}-{day}
    </span>
  );
};

export default DateLabel;