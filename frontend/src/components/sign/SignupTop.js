import React from 'react';
import { AiFillSetting } from 'react-icons/ai';
import { BiArrowBack } from 'react-icons/bi';

function SignupTop(props) {
  return (
    <div className={props.styles.topBox}>
      <a>
        <BiArrowBack className={props.styles.back}></BiArrowBack>
      </a>
      <div className={props.styles.pageTitle}>회원가입</div>
      <a href="">
        <AiFillSetting className={props.styles.fakeSetting}></AiFillSetting>
      </a>
    </div>
  );
}

export default SignupTop;
