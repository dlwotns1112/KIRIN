import React from 'react';
import { AiFillSetting } from 'react-icons/ai';
import { BiArrowBack } from 'react-icons/bi';

function ChallengeTop(props) {
  return (
    <div className={props.styles.topBox}>
      <a>
        <BiArrowBack className={props.styles.back}></BiArrowBack>
      </a>
      <div className={props.styles.pageTitle}>챌린지 제목</div>
      <div href="">
        <AiFillSetting className={props.styles.setting}></AiFillSetting>
      </div>
    </div>
  );
}

export default ChallengeTop;