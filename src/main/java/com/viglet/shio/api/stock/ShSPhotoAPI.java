/*
 * Copyright (C) 2016-2020 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.viglet.shio.api.stock;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viglet.shio.bean.ShPostTinyBean;
import com.viglet.shio.image.colorthief.ColorThief;
import com.viglet.shio.persistence.model.folder.ShFolder;
import com.viglet.shio.persistence.model.post.ShPost;
import com.viglet.shio.persistence.repository.folder.ShFolderRepository;
import com.viglet.shio.persistence.repository.post.ShPostRepository;
import com.viglet.shio.stock.beans.ShSPhotoBean;
import com.viglet.shio.stock.beans.ShSPhotoPreviewBean;
import com.viglet.shio.utils.ShStaticFileUtils;
import com.viglet.shio.website.utils.ShSitesPostUtils;

import io.swagger.annotations.Api;

/**
 * @author Alexandre Oliveira
 * @since 0.3.7
 */
@RestController
@RequestMapping("/api/v2/stock/photos")
@Api(value = "/photos", tags = "Photos", description = "Photos")
public class ShSPhotoAPI {

	@Autowired
	private ShFolderRepository shFolderRepository;
	@Autowired
	private ShPostRepository shPostRepository;
	@Autowired
	private ShStaticFileUtils shStaticFileUtils;
	@Autowired
	private ShSitesPostUtils shSitesPostUtils;
	@GetMapping
	public List<ShSPhotoBean> getPhotos() throws ClientProtocolException, IOException {

		ShFolder shFolder = shFolderRepository.findById("39cef32e-e754-4f1a-991c-c45a1ecebe7c").get();

		List<ShPostTinyBean> shPosts = shPostRepository.findByShFolderTiny(shFolder.getId());

		List<ShSPhotoBean> shSPhotoBeans = new ArrayList<>();

		try {

			for (ShPostTinyBean shPostTinyBean : shPosts) {
				ShPost shPost = shPostRepository.findById(shPostTinyBean.getId()).get();
				File fileImage = shStaticFileUtils.filePath(shPost);
				String imageURL = "http://localhost:2710" + shSitesPostUtils.generatePostLink(shPost);
				BufferedImage image = ImageIO.read(FileUtils.openInputStream(fileImage));
				int height = image.getHeight();
				int width = image.getWidth();

				int[] rgbArray = ColorThief.getColor(image);
				String rgb = String.format("rgb(%d,%d,%d)", rgbArray[0], rgbArray[1], rgbArray[2]);

				System.out.println(imageURL.toString() + " " + rgb);
				ShSPhotoBean shSPhotoBean = new ShSPhotoBean();

				shSPhotoBean.setName(shPost.getTitle());
				shSPhotoBean.setDate(shPost.getDate());
				shSPhotoBean.setPreview_xxs(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setPreview_xs(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setPreview_s(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setPreview_m(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setPreview_l(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setPreview_xl(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setRaw(new ShSPhotoPreviewBean(imageURL.toString(), width, height));
				shSPhotoBean.setDominantColor(rgb);
				shSPhotoBeans.add(shSPhotoBean);
			}
		} catch (JSONException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return shSPhotoBeans;
	}
}
