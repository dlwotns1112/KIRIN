import React, { useEffect, useState } from 'react';
import User from './User.json';
import { useNavigate } from 'react-router-dom';

function Profile(props) {
  const navigate = useNavigate();
  return (
    <div>
      <div className={props.styles.profileBox}>
        <img src={User.img} className={props.styles.userImg}></img>
        <div className={props.styles.userName}>{User.name} </div>
        <button onClick={() => navigate(`/mypage/wallet`)} className={props.styles.myWallet}>
          내지갑
        </button>
      </div>
    </div>
  );
}

export default Profile;
